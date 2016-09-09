module Onboarding.Begin exposing (..)

import Html exposing (..)


-- import Html.App as App
-- import Html.Attributes exposing (style, class, id, src, alt)
-- import Html.Events exposing (onClick)

import Material
import Material.Button as Button
import Material.Options as Options


-- exposing (css)

import Material.Typography as Typo


-- MODEL


type alias Model =
    { mdl : Material.Model
    }


init : Model
init =
    Model Material.model



-- ACTION, UPDATE


type Msg
    = Complete
    | Mdl (Material.Msg Msg)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Complete ->
            ( model, Cmd.none )

        Mdl msg' ->
            Material.update msg' model



-- VIEW


view : Model -> Html Msg
view model =
    Options.div []
        [ Options.styled p
            [ Typo.headline ]
            [ text "We're really excited to have you on board!" ]
        , Options.styled p
            [ Typo.headline ]
            [ text "In order to hold your room, we need to get a few things out of the way first." ]
        , Options.div
            []
            [ Button.render Mdl
                [ 0 ]
                model.mdl
                [ Button.raised
                , Button.colored
                , Button.ripple
                -- , Button.onClick Complete
                ]
                [ text "Get Started" ]
            ]
        ]
