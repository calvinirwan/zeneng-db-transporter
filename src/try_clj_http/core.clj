(ns try-clj-http.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [clojure.edn]
            [clojure.java.jdbc :as sql]))

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
  {:article "/api/v0/article/"
   :learning-tracks "/api/v0/learningtrack/"
   :track-article "/api/v0/trackarticle/"
   :tag "/api/v0/tag/"})

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

(defn get-api [api] 
  (:body (client/get (str (:base-url config) api)
                     headers)))

(defn get-api-all-content [get-api-fn]
  (loop [body get-api-fn
         content '()]
    (if (nil? (:next (:meta body)))
      (concat content (:objects body))
      (recur (get-api(:next (:meta body)))
             (concat content (:objects body))))))

(defn article-mapping [get-api-content-article]
  (let [{:keys [created_datetime
                free_text
                id
                last_modified_datetime
                old_created_by
                old_date_created
                old_id
                premium_text
                resource_uri
                tags
                title]} get-api-content-article]
    {:created_datetime created_datetime
     :free_text free_text
     :id id
     :last_modified_datetime last_modified_datetime
     :old_created_by old_created_by
     :old_date_created old_date_created
     :old_id old_id
     :premium_text premium_text
     :resource_uri resource_uri
     :tags tags
     :title title}))

(defn learning-track-mapping [get-api-content-learning-track]
  (let [{:keys [created_datetime
                description
                id
                last_modified_datetime
                order
                privilege
                publicity
                resource_uri
                tags
                title]} get-api-content-learning-track]
    {:created_datetime created_datetime
     :description description
     :id id
     :last_modified_datetime last_modified_datetime
     :order order
     :privilege privilege
     :publicity publicity
     :resource_uri resource_uri
     :tags tags
     :title title}))

(defn track-article-mapping [get-api-content-track-article]
  (let [{:keys [created_datetime
                free_text
                id
                last_modified_datetime
                learning-track
                order
                premium_text
                privilege
                publicity
                resource_uri
                tags
                title]} get-api-content-track-article]
    {:created_datetime created_datetime
     :free_text free_text
     :description description
     :id id
     :last_modified_datetime last_modified_datetime
     :order order
     :premium_text premium_text
     :privilege privilege
     :publicity publicity
     :resource_uri resource_uri
     :tags tags
     :title title}))

(defn tag-mapping [get-api-content-tag]
  (let [{:keys [id
                 name
                 resource_uri
                 slug]} get-api-content-tag]
    {:id id
     :name name
     :resource_uri resource_uri
     :slug slug}))

(defn insert-article [title id free_text text_premium
                      created_datetime old_created_by
                      url privilege publicity]
  (sql/insert! db :article
               {:title title
                :id id
                :text_free free_text
                :text_premium premium_text
                :created_date created_datetime
                :created_by old_created_by
                :url url
                :privilege privilege
                :publicity publicity}))

(defn insert-learning-track [id name description order privilege publicity]
  (sql/insert! db :learning_track
               {:track_id id
                :track_name name
                :description description
                :order order
                :privilege privilege
                :publicity publicity}))


(defn insert-tag [tag-mapping]
  (sql/insert! db :tag
               {:tag_id (:id tag-mapping)
                :tag_type (:name tag-mapping)}))

(defn finale []
  (map #(insert-tag )))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
