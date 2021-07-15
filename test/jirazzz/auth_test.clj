(ns jirazzz.auth-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest auth
  (tu/respond {:body "html" :status 401})
  (let [r (jirazzz create --summary "hi there" --description "meh")]
    (is (= 1
           (:exit r)))
    (is (re-find
          #"401 Unauthorized; Check the headers in your config."
          (:err r)))))
