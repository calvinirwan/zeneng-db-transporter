(ns try-clj-http.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [clojure.edn]
            [clojure.java.jdbc :as sql]))

(def a "arigatou")

(def config
  (let [config (clojure.edn/read-string (slurp "config.edn"))
        {:keys [key base-url]} config]
    {:key key
     :base-url base-url}))

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

(defn tag-mapping [get-api-content-tag]
  (let [{:keys [id
                 name
                 resource_uri
                 slug]} get-api-content-tag]
    {:id id
     :name name
     :resource_uri resource_uri
     :slug slug}))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
