(ns lock-pdf-values.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [pdfboxing.common :as common]
            [clojure.pprint :refer :all])
  (:import
   [org.apache.pdfbox.pdmodel PDDocumentCatalog]
   [org.apache.pdfbox.pdmodel.common COSObjectable]
   [org.apache.pdfbox.pdmodel.interactive.form PDAcroForm PDField PDSignatureField]
   [java.io IOException])
  (:gen-class))

;; https://pdfbox.apache.org/docs/2.0.7/javadocs

(defn has-value? [field]
  (seq (.getValueAsString field)))

(defn set-fields-flags
  "Assign flags to the fields provided and save the document."
  [filename output doc field-map]
  (try
    (doseq [[field field-bit] field-map]
      (.setFieldFlags field field-bit))
    (.save doc output)
    (catch NullPointerException e
      (str "Error: non existent field provided."))))

(defn set-fields-readonly
  "Set the readonly flag for provided fields in a document"
  [filename output doc fields]
  (let [readonly-flags (repeat (count fields) 1)
        field-map (zipmap fields readonly-flags)]
    (set-fields-flags filename output doc field-map)))

(defn set-fields-with-values-readonly
  "Given a Filename, set only the fields that have been filled in as readonly.
  Return a list of names of the fields that have been modified."
  [filename output]
  (with-open [doc (common/obtain-document filename)]
    (let [catalog (.getDocumentCatalog doc)
          form (.getAcroForm catalog)
          fields (.getFields form)
          fields-to-lock (filter has-value? fields)]
      (set-fields-readonly filename output doc fields-to-lock)
      "Hey I locked some fields"
      fields-to-lock)))

(defn usage [options-summary]
  (->> ["Provided a PDF file with interactive form in it."
        "Will crawl through and set any field with a value to readonly."
        ""
        "Usage: lock-pdf-values -f INPUT-FILE [-o OUTPUT-FILE]"
        ""
        "Options:"
        options-summary
        ""]
       (string/join \newline)))

(def cli-options [["-f" "--filename FILENAME" "File Location"]
                  ["-o" "--output FILENAME" "File Destination (Defaults to input location)"]
                  ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)];
    (when (:help options)
      (println (usage summary))
      (System/exit 0))
    (let [input (:filename options)
          output (or (:output options) input)]
      (println (format "Locking fields for document: %s" input))
      (doseq [(field set-fields-with-values-readonly input output)]
        (println (str "  - " (.getPartialName field))))
      (println (format "Output: %s" output))))
  (System/exit 0))

(def data [
           [[1 2 3][4 5 6]]
           [[3 4 5][9 8 7]]
           ])


(defn adder
  "Adds some shit"
  ([a] (adder a 1 2))
  ([a b] (adder a b 2))
  ([a b c] (+ a b c)))

(def game {:player-pos [1 2]
           :world world-obj
           :viewport :in-game})

(defmulti render-component [game-state] :type)

(defmethod render-component :checkbox [component-state]
  )


(let [output (map render components)]
  (with-html output))

(defmacro tag [tagname & body]
  `(str "<" ~tagname ">"
        ~@body
        "</" ~tagname ">"))


(def cds [{:name "Album",
           :genre "Rock"
           :artist "Wierd Al"
           :rating 9}
          ])

(defn filter-by-artist-and-rating [artist rating]
  (filter (fn [cd] (and
                    (= (:artist cd) artist)
                    (> (:rating cd) rating)))
          cds))

(defn make-comparison [[keyword value]]
  `(= (~keyword "cd") ~value))

(defn make-filter [& filters]
  (doall (map make-comparison (partition 2 filters))))

(defmacro where [& filters]
  `(filter (fn [(gensym cd)] (and
                     ~@(make-filter filters)))
           cds))
