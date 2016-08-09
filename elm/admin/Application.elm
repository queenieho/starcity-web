module Admin.Application exposing (..)

import Html exposing (..)

import Admin.Page as Page


-- MODEL


type alias Model =
    { applicationId : Int
    }


init : Int -> (Model, Cmd Msg)
init id =
    ( Model id
    , Cmd.none
    )


-- UPDATE

type Msg
    = NoOp


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        NoOp ->
            (model, Cmd.none)


-- VIEW

view : Model -> Html Msg
view model =
    div []
        [ h1 [] [ text <| "The id is: " ++ toString model]]
