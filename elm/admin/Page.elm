module Admin.Page exposing ( body )

import Html exposing (..)

import Material.Grid exposing (..)
import Material.Options as Options exposing (styled, cs, css)
import Material.Color as Color


body : String -> Html a -> Html a
body t contents =
  Options.div
    boxed
    [ title t
    , grid [ noSpacing ]
       [ cell
             [ size All 6, size Phone 4 ]
             [ contents ]
       ]
    ]

-- TITLE

title : String -> Html a
title t =
  Options.styled Html.h1
    [ Color.text Color.primary ]
    [ text t ]

-- BODY

boxed : List (Options.Property a b)
boxed =
  [ css "margin" "auto"
  , css "padding-left" "8%"
  , css "padding-right" "8%"
  ]
