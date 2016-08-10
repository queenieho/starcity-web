module Admin.Application.Common exposing (section, definitionList)

import Html exposing (..)

import Material.List as Lists

-- VIEW


-- Card.view
--     [ Color.background (Color.color Color.Pink Color.S500) ]
--     [ Card.title [] [ Card.head [ white ] [ text "Basic Information" ] ]
--     , Card.text
--         [ white ]
--         [ Lists.ul []
--           [ definitionItem "Name" application.name
--           , definitionItem "Email" application.email
--           , definitionItem "Completed At" (toString application.completedAt)
--           ]
--         ]
--     ]

section : String -> Html a -> Html a
section title content =
    div []
        [ h3 [] [ text title ]
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
