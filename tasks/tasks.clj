(ns tasks
  (:refer-clojure :exclude [test])
  (:require
    [babashka.fs :as fs]
    [clojure.java.shell :refer [sh]]
    [clojure.string :as string]
    [clojure.test :as t]))


(defn jirazzz
  [& args]
  (-> args
      vec
      (conj :env {"JIRAZZZ_CONFIG_FILE" "example-config.edn"})
      (->> (apply sh "bb" "./jirazzz"))))


(defn indent
  [n s]
  (string/replace s #"(?m:^)" (string/join "" (repeat n " "))))

(defn clean-string
  [s]
  (string/replace s #"(?m: +$)" ""))


(defn placeholder
  [s marker replacement]
  (-> (string/replace
        s
        (re-pattern
          (str "(?s:(<!-- \\{ " marker " -->).+(<!-- " marker " \\} -->))"))
        (str "$1\n\n" (string/re-quote-replacement (clean-string replacement)) "\n\n$2"))
      (string/split-lines)
      (->> (map #(string/replace % #"^\s+$" ""))
           (string/join "\n"))
      (str "\n")))


(defn usage
  []
  (-> (jirazzz "--help")
      :err))


(def readme "README.md")


(defn docs
  "Regenerate docs"
  []
  (-> readme
      slurp
      (placeholder "jirazzz help" (indent 4 (usage)))
      (placeholder "jirazzz example-config" (str "```clojure\n" (slurp "example-config.edn") "```"))
      (placeholder "jirazzz bb tasks" (indent 4 (:out (sh "bb" "tasks"))))
      (->> (spit readme))))


(defn path->ns
  [p]
  (-> p
      (string/replace #"^test/(.+?)\.clj$" "$1")
      (string/replace #"/" ".")
      (string/replace #"_" "-")
      symbol))


(def test-namespaces
  (-> (fs/glob "test" "**_test.clj")
      (->> (map (comp path->ns str)))))


(defn test
  "Run test suite"
  [patterns]
  (let [tests (cond-> test-namespaces
                (seq patterns)
                (->> (filter (fn [t]
                               (some #(re-find % (str t))
                                     (map re-pattern patterns))))))]
    (doseq [t tests]
      (require t))
    (-> (apply t/run-tests tests)
        (as-> result
          (+ (:fail result) (:error result))
          (if (> result 0) 1 0))
        (System/exit))))
