# ECS

An Immutable Entity Component System

## Usage

```clojure
(require '[darlyngames.ecs :refer :all])

(defrecord AComponent [some values])

(let [entity (create-entity)
      system (-> (create-system)
                 (add-component entity (->AComponent :foo :bar)))]
  (has-component? system entity AComponent) ; => true

  (get-component system entity AComponent) ; => #user.AComponent{:some :foo :values :bar}
  
  (-> system
      (update-component entity AComponent (fn [c] (->AComponent (:values c) (:some c)))))
  
  
  )
```

## License

Copyright © 2015 Darlyn' Games

Distributed under the Eclipse Public License either version 1.0.
