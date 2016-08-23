module Material.Stepper
    exposing
        ( view
        , horizontal
        , vertical
        , linear
        , nonlinear
          -- , Step
          -- , step
          --  Button Helpers
        , next
        , back
        , skip
        , cancel
        )

import Html exposing (Html, Attribute)
import Html.Attributes as Html
import Html.Events as Html


-- import Array

import Material
import Material.Button as Button
import Material.Icon as Icon
import Material.Options as Options exposing (Property, cs, css, when, nop)
import Material.Helpers exposing (map1st, map2nd)


-- import Material.Stepper
-- MODEL


type alias Model =
    { currentStep : Int
    , steps :
        List Step
        -- TODO: Array may be better?
    , mdl : Material.Model
    }


defaultModel : Model
defaultModel =
    { currentStep = 0
    , steps = []
    , mdl = Material.model
    }



-- ACTION/UPDATE


type Msg
    = Complete Int
    | Back Int
    | Skip Int
    | Show Int
    | Mdl (Material.Msg Msg)


update : (Msg -> msg) -> Msg -> Model -> ( Model, Cmd Msg )
update lift msg model =
    case msg of

        Complete idx ->
            ( model, Cmd.none )

        Back idx ->
            ( model, Cmd.none )

        Skip idx ->
            ( model, Cmd.none )

        Show idx ->
            ( model, Cmd.none )

        Mdl msg' ->
            Material.update msg' model


-- Stepper


type Orientation
    = Horizontal
    | Vertical


type StepperType
    = Linear
    | Nonlinear


type alias Config =
    { orientation : Orientation
    , stepperType : Maybe StepperType
    }


defaultStepper : Config
defaultStepper =
    { orientation = Horizontal
    , stepperType = Nothing
    }


view : Model -> List (Property Config Msg) -> List (Html Msg) -> Html Msg
view model properties content =
    let
        ({ config } as summary) =
            Options.collect defaultStepper properties
    in
        Options.apply summary
            Html.ul
            [ cs "mdl-stepper"
            , case config.orientation of
                Horizontal ->
                    cs "mdl-stepper--horizontal"

                Vertical ->
                    cs "mdl-stepper-vertical"
            ]
            []
            (List.map (\step -> step' model config step content) model.steps)


horizontal : Property Config m
horizontal =
    Options.set <| \self -> { self | orientation = Horizontal }


vertical : Property Config m
vertical =
    Options.set <| \self -> { self | orientation = Vertical }


stepperType : StepperType -> Property Config m
stepperType t =
    Options.set <| \self -> { self | stepperType = Just t }


linear : Property Config m
linear =
    stepperType Linear


nonlinear : Property Config m
nonlinear =
    stepperType Nonlinear



-- Step


type Status
    = Completed
    | Error String


type alias Step =
    { index : Int
    , title : String
    , titleMessage : Maybe String
    , status : Maybe Status
    , editable : Bool
    , optional : Bool
    }



-- type alias StepOpts


defaultStep : String -> Maybe String -> (Int -> Step)
defaultStep title message =
    (\idx ->
        { index = idx
        , title = title
        , titleMessage = message
        , status = Nothing
        , editable = False
        , optional = False
        }
    )



-- type alias Step m =
--     ( List (Property (StepConfig m) m), List (Html m), List (Html m) )
-- step : List (Property (StepConfig m) m) -> List (Html m) -> List (Html m) -> Step m
-- step options content actions =
--     ( options, content, actions )


{-| Stepper `step`
-}
step' : Model -> Config -> Step -> List (Html Msg) -> Html Msg
step' model config step content =
    Options.styled Html.li
        [ cs "mdl-step"
        , cs "is-active" `when` (model.currentStep == step.index)
        , cs "mdl-step--editable" `when` step.editable
        , cs "mdl-step--optional" `when` step.optional
        , case step.status of
            Just Completed ->
                cs "mdl-step--completed"

            Just (Error _) ->
                cs "mdl-step--error"

            Nothing ->
                nop
        ]
        [ stepLabel model config step
        , Options.div
            [ cs "mdl-step__content" ]
            content
        , stepActions model config step
        ]


stepActions : Model -> Config -> Step -> Html Msg
stepActions model config step =
    let
        nextIdx buttonIdx =
            buttonIdx + (step.index * buttonIdx)

        backDisabled =
            (step.index == 0) || config.stepperType == Just Linear
    in
        Options.div
            [ cs "mdl-step__actions" ]
            [ Button.render Mdl
                [ nextIdx 0 ]
                model.mdl
                [ back
                , Button.ripple
                , Button.disabled `when` backDisabled
                , Button.onClick (Back step.index)
                ]
                [ Html.text "back" ]
            , if step.optional then
                Button.render Mdl
                    [ nextIdx 1 ]
                    model.mdl
                    [ skip
                    , Button.ripple
                    , Button.onClick (Skip step.index)
                    ]
                    [ Html.text "skip" ]
              else
                Html.div [] []
            ]


stepLabel : Model -> Config -> Step -> Html Msg
stepLabel model config step =
    Html.span
        [ Html.class "mdl-step__label"
        , Html.onClick (Show step.index)
        ]
        [ stepTitle step.title step.titleMessage step.status
        , stepIndicator step.status step.editable step.index
        ]


stepTitle : String -> Maybe String -> Maybe Status -> Html a
stepTitle title message status =
    let
        stepMessage msg =
            Options.span [ cs "mdl-step__title-message" ] [ Html.text msg ]

        message' =
            case ( status, message ) of
                ( Just (Error msg), _ ) ->
                    stepMessage msg

                ( _, Just msg ) ->
                    stepMessage msg

                _ ->
                    Options.span [] []
    in
        Options.span
            [ cs "mdl-step__title" ]
            [ Options.span
                [ cs "mdl-step__title-text" ]
                [ Html.text title ]
            , message'
            ]


stepIndicator : Maybe Status -> Bool -> Int -> Html a
stepIndicator status editable index =
    let
        attrs =
            [ cs "mdl-step__label-indicator-content" ]

        wrapContent content =
            Options.span attrs [ Html.text content ]
    in
        Options.span
            [ cs "mdl-step__label-indicator" ]
            [ (case status of
                Just Completed ->
                    Icon.view
                        (if editable then
                            "edit"
                         else
                            "check"
                        )
                        attrs

                Just (Error msg) ->
                    wrapContent "!"

                Nothing ->
                    wrapContent <| toString (index + 1)
              )
            ]


-- onClick : m -> Property { a | onClick : Maybe (Attribute m) } m
-- onClick x =
--     Options.set (\options -> { options | onClick = Just (Html.onClick x) })


-- index : Int -> Property (StepConfig m) m
-- index idx =
--     Options.set <| \self -> { self | index = idx }


-- title : String -> Property (StepConfig m) m
-- title t =
--     Options.set <| \self -> { self | title = t }


-- titleMessage : String -> Property (StepConfig m) m
-- titleMessage st =
--     Options.set <| \self -> { self | titleMessage = Just st }


-- active : Property (StepConfig m) m
-- active =
--     Options.set <| \self -> { self | active = True }


-- status : Status -> Property (StepConfig m) m
-- status s =
--     Options.set <| \self -> { self | status = Just s }


-- completed : Property (StepConfig m) m
-- completed =
--     status Completed


-- error : String -> Property (StepConfig m) m
-- error msg =
--     status <| Error msg


-- editable : Property (StepConfig m) m
-- editable =
--     Options.set <| \self -> { self | editable = True }


-- optional : Property (StepConfig m) m
-- optional =
--     Options.set <| \self -> { self | optional = True }



-- Actions


next : Button.Property m
next =
    Options.data "stepper-next" ""


back : Button.Property m
back =
    Options.data "stepper-back" ""


cancel : Button.Property m
cancel =
    Options.data "stepper-cancel" ""


skip : Button.Property m
skip =
    Options.data "stepper-skip" ""
