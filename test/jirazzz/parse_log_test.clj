(ns jirazzz.parse-log-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]
    [selmer.filters :as sf]
    [selmer.parser :refer [render]]))


(t/use-fixtures :each tu/with-server-reset)

(sf/add-filter! :indent
                (fn [s n]
                  (string/replace
                    s
                    #"(?m:^)"
                    (apply str (repeat (Integer/parseInt n) " ")))))

(defn git-log
  [& msgs]
  (let [template "commit {{sha}}\nAuthor: {{author}}\nDate:   {{date}}\n\n{{message|indent:4}}\n"]
    (-> msgs
        (->> (map #(render template
                           {:sha "x"
                            :author "y"
                            :date "d"
                            :message %}))
             (string/join "\n")))))


(deftest none
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:transitions tu/responses))

  (let [r (jirazzz parse-log
                   --transition "done"
                   --assignee jzuser
                   :in (git-log "foo" "bar\nbaz"))]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "\n"
           (:out r))
        (:err r)))

  ; FIXME
  (is (= [{:uri (:meta tu/jira-paths)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (:sprint tu/jira-paths)
           :method "get"}]

         (tu/requests))))

(deftest issues
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:transitions tu/responses))

  (let [r (jirazzz parse-log
                   --transition "done"
                   --assignee jzuser
                   :in (git-log "foo\n\ncloses JZ-123" "bar\nbaz"))]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "JZ-123 transitioned to done\n"
           (:out r))
        (:err r)))

  ; FIXME
  (is (= [{:uri (:meta tu/jira-paths)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (:sprint tu/jira-paths)
           :method "get"}
          {:uri (:transitions tu/jira-paths)
           :method "get"}
          {:uri (:transitions tu/jira-paths)
           :method "post"
           :body
           {:transition
            {:id 3}}}]

         (tu/requests))))
