(ns try-clj-http.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [clojure.edn]
            [clojure.java.jdbc :as sql]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c])
  (:import (java.util TimeZone)))

  (TimeZone/setDefault (TimeZone/getTimeZone "GMT"))

(def config
  (let [config (clojure.edn/read-string (slurp "config.edn"))
        {:keys [key base-url db-host db-port db-name user password]} config]
    {:key key
     :base-url base-url
     :db-host db-host
     :db-port db-port
     :db-name db-name
     :user user
     :password password}))

(def db 
  (let [config (clojure.edn/read-string (slurp "config.edn"))
        {:keys [db-host db-port db-name user password]} config]
    {:classname "org.postgresql.Driver" ; must be in classpath
     :subprotocol "postgresql"
     :subname (str "//" db-host ":" db-port "/" db-name)
     ; Any additional keys are passed to the driver
     ; as driver-specific properties.
     :user user
     :password password}))

(def headers 
  {:headers {"Authorization" (:key config)}
   :insecure? true
   :as :json})

(def api 
  {:article "/backoffice/api/v0/article/"
   :learning-track "/backoffice/api/v0/learningtrack/"
   :track-article "/backoffice/api/v0/trackarticle/"
   :tag "/backoffice/api/v0/tag/"})

(def article
  {:id  :id
   :order :order
   :tags :tags})

(def trial '((1 2) (3 4) (5 6)))

(defn fund [list]
  (loop [list list
         fin '()]
    (if (empty? (rest list))
      (concat fin (first list))
      (recur (rest list) (concat fin (first list))))))

(defn create-url [title]
  (->> title 
       (re-seq #"[a-zA-Z0-9]+")
       (interpose "_")
       (apply str)))

(defn get-api [api] 
  (:body (client/get (str (:base-url config) api)
                     headers)))

(defn get-api-all-content [api]
  (let [get-api-fn (get-api api)]
  (loop [body get-api-fn
         content '()]
    (if (nil? (:next (:meta body)))
      (concat content (:objects body))
      (recur (get-api(:next (:meta body)))
             (concat content (:objects body)))))))

(defn time-format [time]
  (let [time (first (clojure.string/split time #"\."))]
    (c/to-sql-time (f/parse (f/formatters :date-hour-minute-second) time))))

(defn insert-article [article-map]
  (sql/insert! db :article
               {:title (:title article-map)
                :id (:id article-map)
                :old_id (:old_id article-map)
                :url (create-url (:title article-map))
                :free_text (:free_text article-map)
                :premium_text (:premium_text article-map)
                :created_by (:old_created_by article-map)
                :created_datetime (time-format(:created_datetime article-map))
                :last_modified_datetime (time-format(:last_modified_datetime article-map))
                :privilege (:privilege article-map)
                :publicity (:publicity article-map)}))

(defn insert-article-tag [article-map]
  (map #(sql/insert! db :article_tag
               {:article_id (:id article-map)
                :tag_id (:id %)}) (:tags article-map)))

(defn article-finale [api]
  (let [article-api (get-api-all-content api)]
    (map #(do (insert-article %) 
              (insert-article-tag %)) article-api)))

(defn insert-learning-track [lt-map]
  (sql/insert! db :learning_track
               {:id (:id lt-map)
                :title (:title lt-map)
                :description (:description lt-map)
                :url (create-url(:title lt-map))
                :created_datetime (time-format(:created_datetime lt-map))
                :last_modified_datetime (time-format(:last_modified_datetime lt-map))
                :learning_track_order (:order lt-map)
                :privilege (:privilege lt-map)
                :publicity (:publicity lt-map)}))

(defn insert-learning-track-tag [lt-map]
  (map #(sql/insert! db :learning_track_tag
               {:learning_track_id (:id lt-map)
                :tag_id (:id %)}) (:tags lt-map)))

(defn learning-track-finale [api]
  (let [lt-api (get-api-all-content api)]
    (map #(do (insert-learning-track %) 
              (insert-learning-track-tag %)) lt-api)))

(defn insert-track-article [ta-map]
  (let [lt (get-api (:learning_track ta-map))]
    (sql/insert! db :track_article
                 {:id (:id ta-map)
                  :title (:title ta-map)
                  :url (create-url(:title ta-map))
                  :article_order (:order ta-map)
                  :created_datetime (time-format (:created_datetime ta-map))
                  :last_modified_datetime (time-format (:last_modified_datetime ta-map))
                  :free_text (:free_text ta-map)
                  :premium_text (:premium_text ta-map)
                  :privilege (:privilege ta-map)
                  :publicity (:publicity ta-map)
                  :track_id (:id lt)})))

(defn insert-track-article-tag [ta-map]
  (map #(sql/insert! db :track_article_tag
               {:track_article_id (:id ta-map)
                :tag_id (:id %)}) (:tags ta-map)))

(defn track-article-finale [api]
  (let [ta-api (get-api-all-content api)]
    (map #(do (insert-track-article %) 
              (insert-track-article-tag %)) ta-api)))

(defn insert-tag [tag-map]
  (sql/insert! db :tag
               {:id (:id tag-map)
                :type (:name tag-map)
                :url (:slug tag-map)}))

(defn tag-finale [api]
  (let [tag-api (get-api-all-content api)]
    (map #(insert-tag %) tag-api)))

(defn insert-tanggalan [tanggal]
  (sql/insert! db :tanggalan
               {:tanggal tanggal}))

(defn exec []
  (do (tag-finale (:tag api))
      (article-finale (:article api))
      (learning-track-finale (:learning-track api))
      (track-article-finale (:track-article api))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
