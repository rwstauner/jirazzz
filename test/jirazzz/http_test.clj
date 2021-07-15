(ns jirazzz.http-test
  (:require
    [clojure.test :refer [deftest testing is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest http
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (doseq [method [:get :post :put :delete]]
    (tu/respond {:match {:uri "/any"
                         :request-method method}
                 :status 200
                 :headers {"content-type" "application/json"}
                 :body (str "{\"received\": \"" (name method) "\"}")}))

  (testing "404"
    (let [r (jirazzz get "/none")]
      (is (= 0
             (:exit r)))
      (is (= "{:body \"\", :status 404}\n"
             (:out r))
          (:err r))))

  ; TODO: always include body/status
  (testing "get"
    (let [r (jirazzz get "/any")]
      (is (= 0
             (:exit r)))
      (is (= "{:received \"get\"}\n"
             (:out r))
          (:err r))))

  (testing "post"
    (let [r (jirazzz post "/any" "{:json {:get 1}}")]
      (is (= 0
             (:exit r))
          (:err r))
      (is (= "{:received \"post\"}\n"
             (:out r))
          (:err r))))

  (testing "put"
    (let [r (jirazzz put "/any" "{:json {:put 1}}")]
      (is (= 0
             (:exit r))
          (:err r))
      (is (= "{:received \"put\"}\n"
             (:out r))
          (:err r))))

  (testing "delete"
    (let [r (jirazzz delete "/any" --output-format json)]
      (is (= 0
             (:exit r))
          (:err r))
      (is (= "{\"received\":\"delete\"}\n"
             (:out r))
          (:err r))))

  (is (= [{:uri "/none"
           :method "get"}

          {:uri "/any"
           :method "get"}

          {:uri "/any"
           :method "post"
           :body {:get 1}}

          {:uri "/any"
           :method "put"
           :body {:put 1}}

          {:uri "/any"
           :method "delete"}]
         (tu/requests))))
