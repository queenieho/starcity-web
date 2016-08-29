module Onboarding.Loading exposing (..)

import Html exposing (..)
import Material.Options as Options exposing (css)
import Material.Spinner as Spinner
import Onboarding.Common as Common


-- VIEW


view : Html m
view =
    Common.container' "Loading..."
        [ Options.div
            -- TODO: css class, adjust padding w/ media query
            [ Options.center
            , css "padding-top" "120px"
            , css "padding-bottom" "120px"
            ]
            [ Spinner.spinner
                [ Spinner.active True
                , Spinner.singleColor True
                , css "width" "80px"
                , css "height" "80px"
                ]
            ]
        ]
