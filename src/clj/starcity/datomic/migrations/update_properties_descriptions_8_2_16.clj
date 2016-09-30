(ns starcity.datomic.migrations.update-properties-descriptions-8-2-16)

(def update-properties-descriptions
  {:seed/update-properties-descriptions-8-2-16
   {:txes [[{:db/id                [:property/internal-name "2072mission"]
             :property/description "In the heart of the Mission neighborhood, we've converted this three-story building to offer our community members over 1,000 square feet in communal space. Members share an industrial-sized kitchen with multiple cooking stations, a bar and social lounge, media rooms and bike storage. Members also have access to a back patio to soak in the afternoon sunshine. Each member gets their own private bedroom for rest and relaxation. As with all of our communities, all common and private areas are furnished for easy move-in. This community has 20 private units."}
            {:db/id                [:property/internal-name "229ellis"]
             :property/description "Our Union Square location is a four-story, masonry structure completed in 1910. The building was built as a bathhouse and continued to be used as such until the mid-1980's. We're converting the top three floors into private units for our members and the ground-level offerings will be determined by the initial members of the community. Sign up now to be a part of this communal building and have a say on the function of communal spaces! This community will have 56 private units."}]]
    :requires [:starcity/add-property-schema
               :starcity/seed-mission
               :starcity/seed-union-square]}})
