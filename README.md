# ECS

An Immutable Entity Component System

## Usage

```clojure
(require '[darlyngames.ecs :refer :all])

(defrecord AComponent [some values])

(def e-id (create-entity))
(def system (-> (create-system)
                (add-component entity (->AComponent :foo :bar)))

(has-component? system entity AComponent)
; => true

(get-component system entity AComponent)
; => #user.AComponent{:some :foo :values :bar}

(-> system
    (update-component entity 
                      AComponent
                      (fn [c v] (->AComponent v (:values c)))
                      :baz)
    (get-component entity AComponent))
; => #user.AComponent{:some :baz :values :bar}
  
```

## License

Copyright © 2015 Darlyn' Games

Distributed under the Eclipse Public License either version 1.0.
