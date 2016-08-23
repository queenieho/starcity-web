port module Onboarding.SecurityDeposit exposing (..)

import Html exposing (..)
import Html.Attributes exposing (class)
import Material
import Material.Button as Button
import Material.Grid exposing (grid, cell, size, offset, Device(..))
import Material.Toggles as Toggles
import Material.Options as Options


-- exposing (css, when, cs)

import Onboarding.Common as Common


-- MODEL


type PaymentMethod
    = ACH
    | Check


type DepositAmount
    = Partial
    | Full


type alias Model =
    { choice : Maybe PaymentMethod
    , amount : Maybe DepositAmount
    , mdl : Material.Model
    }


init : ( Model, Cmd Msg )
init =
    ( Model Nothing Nothing Material.model, Cmd.none )



-- ACTION, UPDATE


type Msg
    = ChoosePayment PaymentMethod
    | ChooseAmount DepositAmount
    | VerifyInstantly
    | VerifyMicrodeposits
    | LinkSuccess String
    | Mdl (Material.Msg Msg)


port openLink : Maybe String -> Cmd msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChoosePayment newMethod ->
            { model | choice = Just newMethod } ! []

        ChooseAmount newAmount ->
            { model | amount = Just newAmount } ! []

        VerifyInstantly ->
            ( model, openLink Nothing )

        VerifyMicrodeposits ->
            model ! []

        LinkSuccess token ->
            model ! []

        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model



-- SUBSCRIPTIONS


port linkSuccess : (String -> msg) -> Sub msg
port linkExit : (() -> msg) -> Sub msg
port linkLoad : (() -> msg) -> Sub msg


subscriptions : Model -> Sub Msg
subscriptions model =
    linkSuccess LinkSuccess



-- VIEW


view : Model -> Html Msg
view model =
    Common.container "Security Deposit" <|
        content model


content : Model -> List (Html Msg)
content model =
    let
        startingContent =
            Common.section "How would you like to pay your security deposit?"
                "To hold your room at PROPERTY NAME, we need to collect your rental deposit. There are two options:"
                [ achToggle model, checkToggle model ]
    in
        case model.choice of
            Nothing ->
                [ startingContent ]

            Just choice ->
                [ startingContent
                , makePaymentSection model choice
                ]


makePaymentSection : Model -> PaymentMethod -> Html Msg
makePaymentSection model choice =
    let
        ( title, desc, content ) =
            case choice of
                ACH ->
                    ( "Great! Next we'll need to verify your bank account."
                    , """There are two options to verify your account. For select banks can securely verify
                       your bank account nearly instantly. Choose the...
                       """
                    , [ verificationOptions model ]
                    )

                Check ->
                    ( "You can make your check payable using following information."
                    , "Please note that your room will only be held for 10 days, and stuff."
                    , []
                    )
    in
        Common.section title desc content


verificationOptions : Model -> Html Msg
verificationOptions model =
    grid
        [ Options.center ]
        [ cell
            [ size All 6
            , Options.center
            ]
            [ Button.render Mdl
                [ 0 ]
                model.mdl
                [ Button.raised
                , Button.colored
                , Button.ripple
                , Button.onClick VerifyInstantly
                ]
                [ text "Verify Instantly" ]
            ]
        , cell
            [ size All 6
            , Options.center
            ]
            [ Button.render Mdl
                [ 1 ]
                model.mdl
                [ Button.colored
                , Button.ripple
                , Button.onClick VerifyMicrodeposits
                ]
                [ text "Microdeposits" ]
            ]
        ]


payByACH : Model -> Html Msg
payByACH model =
    let
        toggle idx msg txt val =
            div []
                [ Toggles.radio Mdl
                    [ idx ]
                    model.mdl
                    [ Toggles.value val
                    , Toggles.group "payment-type"
                    , Toggles.ripple
                    , Toggles.onClick msg
                    ]
                    [ text txt ]
                ]
    in
        div []
            [ toggle 2
                (ChooseAmount Partial)
                "Pay $500 now and the rest at move-in."
                (model.amount == Just Partial)
            , toggle 3
                (ChooseAmount Full)
                "Pay full amount now."
                (model.amount == Just Full)
            ]


achToggle : Model -> Html Msg
achToggle model =
    div [ class "ob-section__toggle" ]
        [ Toggles.radio Mdl
            [ 0 ]
            model.mdl
            [ Toggles.value (model.choice == Just ACH)
            , Toggles.group paymentRadioGroup
            , Toggles.ripple
            , Toggles.onClick (ChoosePayment ACH)
            ]
            [ text "Pay with ACH" ]
        ]


checkToggle : Model -> Html Msg
checkToggle model =
    div []
        [ Toggles.radio Mdl
            [ 1 ]
            model.mdl
            [ Toggles.value (model.choice == Just Check)
            , Toggles.group paymentRadioGroup
            , Toggles.ripple
            , Toggles.onClick (ChoosePayment Check)
            ]
            [ text "Pay by Check" ]
        ]


paymentRadioGroup : String
paymentRadioGroup =
    "payment-choice"
