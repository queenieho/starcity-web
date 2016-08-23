module Onboarding.Common exposing (..)

import Html exposing (..)
import Html.Attributes exposing (class)

import Page

import Material.Elevation as Elevation
import Material.Grid exposing (grid, cell, size, offset, Device(..))
import Material.Options as Options exposing (cs)

container : String -> List (Html m) -> Html m
container title content =
    Options.div
        Page.boxed
        [ grid []
            [ cell
                [ size Desktop 8
                , offset Desktop 2
                , size Tablet 8
                , size Phone 4
                , Elevation.e3
                , cs "content-card"
                ]
                [ Options.div
                    [ cs "content-card__header"
                    , Elevation.e2
                    ]
                    [ h2 [] [ text title ] ]
                , div [ class "content-card__content" ] content
                ]

            ]
        ]

section : String -> String -> List (Html m) -> Html m
section title subtext content =
    div
        [ class "ob-section" ]
        [ h3
            [ class "ob-section__title" ]
            [ text title ]
        , div
            [ class "ob-section__description " ]
            [ p [] [ text subtext ] ]
        , div [ class "ob-section__content" ] content
        ]
