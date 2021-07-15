(ns jirazzz.server-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(def status+body (juxt :status (comp tu/read-body :body)))


(deftest sanity
  (is (= [404 ""]
         (-> (tu/request :get "/foo")
             status+body)))

  (tu/respond {:body "foo" :status 200})
  (is (= [200 "foo"]
         (-> (tu/request :get "/bar")
             status+body)))

  (is (= [{:uri "/foo" :body nil :query-string nil}
          {:uri "/bar" :body nil :query-string nil}]
         (-> (tu/requests))))

  (tu/reset)

  (tu/respond {:match {:uri "/qux"}
               :body "q" :status 200})
  (tu/respond {:match {:uri "/hoge" :query-string "q=1"}
               :body "q2" :status 201})
  (is (= [404 ""]
         (-> (tu/request :get "/baz")
             status+body)))

  (is (= [200 "q"]
         (-> (tu/request :get "/qux")
             status+body)))
  (is (= [201 "q2"]
         (-> (tu/request :get "/hoge" :query-params {:q 1})
             status+body)))
  (is (= [404 ""]
         (-> (tu/request :get "/hoge")
             status+body)))

  (is (= [{:uri "/baz" :body nil :query-string nil}
          {:uri "/qux" :body nil :query-string nil}
          {:uri "/hoge" :body nil :query-string "q=1"}
          {:uri "/hoge" :body nil :query-string nil}]
         (-> (tu/requests)))))
