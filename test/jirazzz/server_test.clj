(ns jirazzz.server-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(def status+body (juxt :status :body))


(deftest sanity
  (is (= [404 ""]
         (-> (tu/request :get "/foo")
             status+body)))

  (tu/respond {:body "foo" :status 200})
  (is (= [200 "foo"]
         (-> (tu/request :get "/bar")
             status+body)))

  (is (= [{:method "get" :uri "/foo"}
          {:method "get" :uri "/bar"}]
         (-> (tu/requests)))
      "basic")

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

  (tu/respond {:match {:uri "/piyo" :body "👋"}
               :body "👌" :status 204})
  (is (= [204 "👌"]
         (-> (tu/request :post "/piyo" :body "👋")
             status+body)))
  (is (= [404 ""]
         (-> (tu/request :post "/piyo" :body "x")
             status+body)))

  (is (= [{:method "get"  :uri "/baz"}
          {:method "get"  :uri "/qux"}
          {:method "get"  :uri "/hoge" :query-string "q=1"}
          {:method "get"  :uri "/hoge"}
          {:method "post" :uri "/piyo" :body "👋"}
          {:method "post" :uri "/piyo" :body "x"}]
         (-> (tu/requests)))))
