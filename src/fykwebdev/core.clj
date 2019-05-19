(ns fykwebdev.core
  (:require [toucan.db :as db]
            [toucan.models :as models]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.api.sweet :refer [api routes]]
            [fykwebdev.todolist :refer [todolist-routes]]
            [fykwebdev.survey :refer [survey-routes]]
            [ring.middleware.cors :refer [wrap-cors]])
  (:gen-class))

(def db-spec
  {:dbtype "postgres"
   :dbname "fykwebdev"})

(def swagger-conf
  {:ui "/swagger"
   :spec "/swagger.json"
   :options {:ui {:validatorUrl nil}
             :data {:info {:version "1.0.0", :title "Restful Microservice CRUD API"}}}})

(def app (-> (api {:swagger swagger-conf} (apply routes todolist-routes survey-routes))
             (wrap-cors :access-control-allow-origin  [#".*"]
                        :access-control-allow-methods [:get :post :put :delete])))

(defn -main
  [& args]
  (println "Hello, World!")
  (db/set-default-db-connection! db-spec)

  (models/set-root-namespace! 'fykwebdev.models)
  (run-jetty app {:port 3000}))

