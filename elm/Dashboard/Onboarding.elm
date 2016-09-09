module Dashboard.Onboarding exposing (..)

import Html exposing (..)
-- import Html.Attributes exposing (style, class, id, src, alt)
-- import Html.Events exposing (onClick)
import Platform.Cmd as Cmd

import Dashboard.Page as Page

import Material
import Material.Button as Button
import Material.Grid exposing (grid, cell, size, Device(..))
import Material.Options exposing (when)


-- MODEL


type alias Model =
    { mdl : Material.Model
    }


init : (Model, Cmd Msg)
init =
    ( { mdl = Material.model
      }
    , Cmd.none
    )

-- ACTION/UPDATE

type Msg
    = NoOp


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            model ! []


-- VIEW


view : Model -> Html Msg
view model =
    grid [] [ cell
                  [ size All 12]
                  [ Html.text "Hello"]
            ]
