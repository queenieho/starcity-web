(ns starcity.datomic.migrations.properties-schema-8-2-16.update-properties-description-copy
  (:require [starcity.datomic.migrations :refer [defnorms]]
            [starcity.models.util :refer [one]]
            [datomic.api :as d]))

(defnorms update-properties-description-copy
  :txes [{:db/id                [:property/internal-name "2072mission"]
          :property/description "In the heart of the Mission neighborhood, we've converted this three-story building to offer our community members over 1,000 square feet in communal space. Members share an industrial-sized kitchen with multiple cooking stations, a bar and social lounge, media rooms and bike storage. Members also have access to a back patio to soak in the afternoon sunshine. Each member gets their own private bedroom for rest and relaxation. As with all of our communities, all common and private areas are furnished for easy move-in. This community has 20 private units."}
         {:db/id                [:property/internal-name "229ellis"]
          :property/description "Our Union Square location is a four-story, masonry structure completed in 1910. The building was built as a bathhouse and continued to be used as such until the mid-1980's. We're converting the top three floors into private units for our members and the ground-level offerings will be determined by the initial members of the community. Sign up now to be a part of this communal building and have a say on the function of communal spaces! This community will have 56 private units."}
         {:db/id                [:property/internal-name "361turk"]
          :property/description "This is a ground-up development in the historic Tenderloin neighborhood. It will offer over 200 new housing units with lots of communal space. All units will have state of the art private baths and cooking facilities. Units will range from 200 to 500 square feet and there will be ~7,000 square feet of communal and retail space. There will be shared roof decks and double height lounges on every other floor. We are implementing state of the art green technologies with very low energy and water costs. Sign up now to have a say in the development of communal areas and to engage with early community members!"}]
  ;; NOTE: Shouldn't have to do this.
  :requires [seed-historic-tenderloin])
