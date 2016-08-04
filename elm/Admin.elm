module Admin exposing (..)

import Html.App as App
import Html exposing (..)
import Html.Lazy
import Html.Attributes exposing (href, class, style)
import Array exposing (Array)
import Dict exposing (Dict)

import Material
import Material.Color as Color
import Material.Options as Options -- exposing (css, when)
import Material.Typography as Typography
import Material.Layout as Layout
import Material.Helpers exposing (lift)

import Admin.Applications


-- MODEL


type alias Model =
    { selectedTab : Int
    , mdl : Material.Model -- boilerplate model for Mdl components
    , applications : Admin.Applications.Model

    }

model: Model
model =
    { selectedTab = 0
    , mdl = Material.model -- boilerplate: always use this initial mdl model store
    , applications = Admin.Applications.model
    }

-- ACTION, UPDATE


type Msg
    = SelectTab Int
    | Mdl (Material.Msg Msg)
    | ApplicationsMsg Admin.Applications.Msg
      -- Boilerplate: Msg clause for internal Mdl messages

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SelectTab t ->
            ( { model | selectedTab = t } , Cmd.none )

        ApplicationsMsg msg' ->
            lift .applications (\m sm -> {m | applications = sm}) ApplicationsMsg Admin.Applications.update msg' model

        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model

-- VIEW

tabs : List (String, String, Model -> Html Msg)
tabs =
    [ ("Applications", "applications", .applications >> Admin.Applications.view >> App.map ApplicationsMsg) ]

tabTitles : List (Html a)
tabTitles =
    List.map (\ (x,_,_) -> text x) tabs

tabViews : Array (Model -> Html Msg)
tabViews =
    List.map (\ (_,_,x) -> x) tabs
        |> Array.fromList

tabUrls : Array String
tabUrls =
    List.map (\ (_,url,_) -> url) tabs
        |> Array.fromList

urlTabs : Dict String Int
urlTabs =
    List.indexedMap (\idx (_,url,_) -> (url, idx)) tabs
        |> Dict.fromList

e404 : Model -> Html Msg
e404 _ =
  div
    [
    ]
    [ Options.styled Html.h1
        [ Options.cs "mdl-typography--display-4"
        , Typography.center
        ]
        [ text "404" ]
    ]

header : Model -> List (Html Msg)
header model =
    [ Layout.row []
          [ Layout.title [] [ text "Starcity Admin" ]
          , Layout.spacer
          , Layout.navigation []
              [ Layout.link
                    [ Layout.href "/logout" ]
                    [ span [] [ text "Log Out" ] ]
              ]
          ]
    ]

view : Model -> Html Msg
view = Html.Lazy.lazy view'

view' : Model -> Html Msg
view' model =
    let
        top =
            (Array.get model.selectedTab tabViews |> Maybe.withDefault e404) model
    in
    Layout.render Mdl model.mdl
        [ Layout.fixedHeader
        , Layout.selectedTab model.selectedTab
        , Layout.onSelectTab SelectTab
        ]
    { header = header model
    , drawer = []
    , tabs = (tabTitles, [ Color.background (Color.color Color.Green Color.S400) ])
    , main = [ top ]
    }


-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
    Layout.subs Mdl model.mdl

-- MAIN


main : Program Never
main =
    App.program
        { init = ( { model | mdl = Layout.setTabsWidth 400 model.mdl }
                 , Layout.sub0 Mdl
                 )
        , view = view
        , update = update
        , subscriptions = subscriptions
        }
