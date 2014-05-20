(ns try-clj-http.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [clojure.edn]))

(def a "arigatou")

(def config
  (let [config (clojure.edn/read-string (slurp "config.edn"))
        {:keys [key base-url]} config]
    {:key key
     :base-url base-url}))

(def article
  {:id  :id
   :order :order
   :tags :tags})

(defn get-article [] 
  (:body (client/get (str (:base-url config) "/api/v0/article/")
  {:headers {"Authorization" (:key config)}
   :insecure? true
   :as :json})))

(defn get-learning-track [] 
  (:body (client/get (str (:base-url config) "/api/v0/learningtrack/")
  {:headers {"Authorization" (:key config)}
   :insecure? true
   :as :json})))

(defn get-track-article [] 
  (:body (client/get (str (:base-url config) "/api/v0/trackarticle/")
  {:headers {"Authorization" (:key config)}
   :insecure? true
   :as :json})))

(defn get-tag [] 
  (:body (client/get (str (:base-url config) "/api/v0/tag/")
  {:headers {"Authorization" (:key config)}
   :insecure? true
   :as :json})))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
