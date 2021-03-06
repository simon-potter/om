(ns examples.mouse.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.events :as events]
            [cljs.core.async :as async :refer [>! <! put! chan]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:import [goog.events EventType]))

(enable-console-print!)

(defn listen [el type]
  (let [out (chan)]
    (events/listen el type #(put! out %))
    out))

(om/root
  {:mouse nil}
  (fn [app node]
    (reify
      om/IWillMount
      (will-mount [_]
        (let [mouse-chan
              (async/map
                (fn [e] [(.-clientX e) (.-clientY e)])
                [(listen js/window EventType/MOUSEMOVE)])]
          (go (while true
                (om/update! app assoc :mouse (<! mouse-chan))))))
      om/IRender 
      (render [_]
        (dom/p nil
          (when-let [pos (:mouse app)]
            (pr-str (:mouse app)))))))
  (.getElementById js/document "app"))
