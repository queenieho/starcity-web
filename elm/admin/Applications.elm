module Admin.Applications exposing (..)

import Html exposing (..)

import Material.Table as Table


-- MODEL


type alias Model =
    { applications : List Application }

type alias Application =
    { id : Int
    , name : String
    , email : String
    , completedAt : String
    }

seedApplications : List Application
seedApplications =
    [
     { id = 123
     , name = "Josh Lehman"
     , email = "jalehman37@gmail.com"
     , completedAt = "August 4th, 2016"
     }
    ]

model : Model
model =
    { applications = seedApplications
    }


-- MESSAGES


type Msg
    = NoOp



-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )



-- SUBSCRIPTIONS


-- subscriptions : Model -> Sub Msg
-- subscriptions model =
--     Sub.none



-- VIEW

applicationRow : Application -> Html a
applicationRow {id,name,email,completedAt} =
    Table.tr []
        [ Table.td [] [ text (toString id) ]
        , Table.td [] [ text name ]
        , Table.td [] [ text email ]
        , Table.td [] [ text completedAt ]
        ]

view : Model -> Html Msg
view model =
    let
        table =
            Table.table []
                [ Table.thead []
                      [ Table.tr []
                            [ Table.th [] [ text "ID" ]
                            , Table.th [] [ text "Name" ]
                            , Table.th [] [ text "Email" ]
                            , Table.th [] [ text "Completed At" ]
                            ]
                      ]
                , Table.tbody []
                     (List.map applicationRow model.applications)
                ]


    in
    div []
        [ h2 [] [ text "Applications" ]
        , table
        ]



-- -- MAIN


-- main : Program Never
-- main =
--     Html.App.program
--         { init = init
--         , view = view
--         , update = update
--         , subscriptions = subscriptions
--         }
