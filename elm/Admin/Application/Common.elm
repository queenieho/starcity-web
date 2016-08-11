module Admin.Application.Common exposing (section, definitionList)

import Html exposing (..)

-- import Material.Card as Card
import Material.Color as Color
import Material.List as Lists
import Material.Options as Options exposing (css)

-- VIEW

white : Options.Property c m
white =
    Color.text Color.white


section : String -> Html a -> Html a
section title content =
    -- Card.view
    --     [ Color.background (Color.color Color.Cyan Color.S100)
    --     , css "width" "100%"
    --     ]
    --     [ Card.title [] [ Card.head [] [ text title ] ]
    --     , Card.text [] [ content ]
    --     ]
    Options.div [ css "border" "1px solid grey"
                , css "padding" "15px 15px"]
        [ h3 [] [text title ]
        , content
        ]

definitionList : List (String, String) -> Html a
definitionList definitions =
    Lists.ul []
        <| List.map definitionItem definitions

definitionItem : (String, String) -> Html a
definitionItem (term,definition) =
    Lists.li [ Lists.withBody ]
        [ Lists.content []
              [ text term
              , Lists.body [] [ text definition ] ] ]
