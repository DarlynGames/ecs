(ns darlyngames.ecs
  (:import [java.util UUID]))

(defprotocol IEntityComponentSystem
  "The basic set of functionality required to be our ECS."
  (add-component  [system e-id c] "Associates an instance of a Component with the given entity.")
  (get-components [system e-id] "Returns all components associated with the given entity.")
  (get-component  [system e-id k] [system e-id k not-found] "Get a specific type of component associated with the given entity, returning `nil` if it doesn't exist or, optionally, `not-found`.")
  (entities-with-component [system k] "Retrieve a collection of all entites that have the given component type.")
  (all-components [system] "Retrieve a collection of all components in the system.")
  (remove-component [system e-id k] "Disassociate the given component type from the given entity.")
  (remove-entity [system e-id] "Remove an entity and all of it's components from the system.")
  (has-component? [system e-id k] "`true` if the given entity has the given component type associated with it, `false` otherwise."))

(defn create-entity []
  (UUID/randomUUID))

(defn component-type [c]
  (class c))

(defn get-owner [component]
  (:e-id (meta component)))

(defrecord EntityComponentSystem [entity->components component->entities]
  IEntityComponentSystem
  (add-component [system e-id c]
    (let [k (component-type c)]
      (->EntityComponentSystem
        (assoc-in entity->components [e-id k] (with-meta c {:e-id e-id}))
        (update-in component->entities [k] #(if % (conj % e-id) #{e-id})))))

  (get-components [_ e-id]
    (vals (get entity->components e-id)))

  (get-component [s e-id k]
    (get-component s e-id k nil))

  (get-component [_ e-id k not-found]
    (get-in entity->components [e-id k] not-found))

  (has-component? [_ e-id k]
    (contains? (get entity->components e-id) k))

  (entities-with-component [_ k]
    (get component->entities k))

  (all-components [_]
    (mapcat #(vals (second %)) entity->components))

  (remove-component [_ e-id k]
    (->EntityComponentSystem (update-in entity->components  [e-id] #(dissoc % k))
                             (update-in component->entities [k]    #(disj % e-id))))
  (remove-entity [_ e-id]
    (let [ks (map #(class (second %)) (get entity->components e-id))]
      (->EntityComponentSystem
        (dissoc entity->components e-id)
        (reduce (fn [c->e k]
                  (update-in c->e [k] #(disj % e-id)))
                component->entities
                ks)))))

(defn update-component 
  "Update an Entity's Component.
  `f` is applied with the Component as the first argument and `args` appended.
  If the Entity does not have an instance of the Component type, the first arg to `f` is `nil`.
  `f` is expected to return a Component of the same type as was queried."
  [system e-id k f & args]
  (let [new-component (apply f (get-component system e-id k) args)]
    (if (= (class new-component) k)
      (add-component system e-id new-component)
      (throw (Exception.
               (str "Update function expected to return `"
                    k
                    "` but returned `"
                    (class new-component)
                    "`"))))))

(defn add-components [system e-id components]
  "Add a bunch of components to an entity in one call."
  (reduce (fn [s c]
            (add-component s e-id c))
          system
          components))

(defn create-system []
  (->EntityComponentSystem {} {}))

