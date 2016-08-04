module Main exposing (..)

import Html.App as App
import Html exposing (..)
import Html.Attributes exposing (href, class, style)

import Material
import Material.Scheme
import Material.Button as Button
import Material.Options exposing (css)


-- MODEL


type alias Model =
    { count : Int
    , mdl : Material.Model -- boilerplate model for Mdl components
    }

model: Model
model =
    { count = 0
    , mdl = Material.model -- boilerplate: always use this initial mdl model store
    }

-- ACTION, UPDATE


type Msg
    = Increase
    | Reset
    | Mdl (Material.Msg Msg)
      -- Boilerplate: Msg clause for internal Mdl messages

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Increase ->
            ( { model | count = model.count + 1 }
            , Cmd.none
            )

        Reset ->
            ( { model | count = 0 }
            , Cmd.none
            )

        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model

-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ style [ ("padding", "2rem") ] ]



-- MAIN


main : Program Never
main =
    App.program
        { init = ( model, Cmd.none )
        , view = view
        , update = update
        , subscriptions = always Sub.none
        }
