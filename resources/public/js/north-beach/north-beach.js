$(document).ready(function() {

  function makeStairs(canvas, stairs, stairsStyle) {
    const stairsObj = [];

    stairs.forEach((item) => {
      stairsObj.push(new linea.Stairs(canvas, item.origin, item.outline, 6, item.vertical, stairsStyle));
    });

    return stairsObj;
  }

  function makeInteriorWall(canvas, interiorWalls, interiorWallStyle) {
    const interiorWallsObj = [];

    interiorWalls.forEach((item) => {
      interiorWallsObj.push(new linea.InteriorWall(canvas, item.origin, item.outline, item.code, interiorWallStyle));
    });

    return interiorWallsObj;
  }

  function makeNightTable(canvas, nightTables, nightTableStyle, labelStyle) {
    const nightTablesObj = [];

    nightTables.forEach((item) => {
      nightTablesObj.push(new linea.NightTable(canvas, item.origin, item.outline, item.code, nightTableStyle, item.label, labelStyle));
    });

    return nightTablesObj;
  }

  function makeBed(canvas, beds, bedStyle, labelStyle) {
    const bedsObj = [];

    beds.forEach((item) => {
      bedsObj.push(new linea.Bed(canvas, item.origin, item.outline, item.code, bedStyle, item.label, labelStyle));
    });

    return bedsObj;
  }

  function makeDoor(canvas, doors, doorStyle, doorProjStyle, doorStopStyle) {
    const doorsObj = [];

    doors.forEach((item) => {
      doorsObj.push(new linea.Door(canvas, item.origin, item.outline, 90, item.clockwise, item.code, doorStyle, doorStopStyle, doorProjStyle));
    });

    return doorsObj;
  }

  function makeSlidingDoor(canvas, slidingDoors, slidingDoorStyle, doorProjStyle) {
    const slidingDoorsObj = [];

    slidingDoors.forEach((item) => {
      slidingDoorsObj.push(new linea.SlidingDoor(canvas, item.origin, item.outline, item.code, slidingDoorStyle, doorProjStyle));
    });

    return slidingDoorsObj;
  }

  function makeWindows(canvas, windows, windowStyle) {
    const windowsObj = [];

    windows.forEach((item) => {
      windowsObj.push(new linea.Window(canvas, item.origin, item.outline, item.code, windowStyle));
    });

    return windowsObj;
  }

  function makeDresser(canvas, dressers, dresserStyle, labelStyle) {
    const dressersObj = [];

    dressers.forEach((item) => {
      dressersObj.push(new linea.Dresser(canvas, item.origin, item.outline, item.code, dresserStyle, item.label, labelStyle));
    });

    return dressersObj;
  }

  function getFeature(object, feature) {
    const list = [];
    object.forEach((item) => {
      if (item.type === feature) { list.push(item); }
    });

    return list;
  }

  function write(floor, origin, labels, style) {
    labels.forEach((item) => {
      floor.writeLabel(item.x + origin.x, item.y + origin.y, item.label, style);
    });
  };

  const zeroOrigin = { x: 0, y: 0 };

  // #########################################################################
  // Floor 1
  // #########################################################################

  if ($('#nott-floor1').length) {
    const canvas1 = new linea.LineaCanvas('#nott-floor1', -5, -5, 250, 700);
    const nb = new linea.Floorplan(canvas1, nbFloorOne.unit.origin, nbFloorOne.room.outline, nbFloorOne.unit.code, style.floorOutline.default);

    let nbWalls = getFeature(nbFloorOne.features, 'interiorWall');
    nbWalls = makeInteriorWall(canvas1, nbWalls, style.interiorWallStyle.default);
    let nbWin = getFeature(nbFloorOne.features, 'window');
    nbWin = makeWindows(canvas1, nbWin, style.windowStyle.default);
    let nbDoor = getFeature(nbFloorOne.features, 'door');
    nbDoor = makeDoor(canvas1, nbDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    let nbSliding = getFeature(nbFloorOne.features, 'slidingDoor');
    nbSliding = makeSlidingDoor(canvas1, nbSliding,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    nb.addFeature(zeroOrigin, nbWalls, nbWin, nbDoor, nbSliding);

    const wall = new linea.Room(canvas1, zeroOrigin, nbOneWall.outline, nbOneWall.unit.code, style.roomOutline.orange);
    let stairs1 = getFeature(nbOneWall.features, 'stairs');
    stairs1 = makeStairs(canvas1, stairs1, style.stairStyle.default);
    wall.addFeature(zeroOrigin, stairs1);

    nb.addRoom(nbOneWall.unit.origin, wall);

    const deck = new linea.Room(canvas1, zeroOrigin, nbOneDeck.outline, nbOneDeck.unit.code, style.roomOutline.green);
    nb.addRoom(nbOneDeck.unit.origin, deck);

    const bedOne = new linea.Room(canvas1, zeroOrigin, nbOneBed1.outline, nbOneBed1.unit.code, style.roomOutline.red);
    let bedOneWindow = getFeature(nbOneBed1.features, 'window');
    bedOneWindow = makeWindows(canvas1, bedOneWindow, style.windowStyle.default);
    let bedOneDoor = getFeature(nbOneBed1.features, 'door');
    bedOneDoor = makeDoor(canvas1, bedOneDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    bedOne.addFeature(zeroOrigin, bedOneWindow, bedOneDoor);
    nb.addRoom(nbOneBed1.unit.origin, bedOne);

    const laundry = new linea.Room(canvas1, zeroOrigin, nbOneLaundry.outline, nbOneLaundry.unit.code, style.roomOutline.blue);
    let laundryDoor = getFeature(nbOneLaundry.features, 'door');
    laundryDoor = makeDoor(canvas1, laundryDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    laundry.addFeature(zeroOrigin, laundryDoor);
    nb.addRoom(nbOneLaundry.unit.origin, laundry);

    const toilet = new linea.Room(canvas1, zeroOrigin, nbOneToilet.outline, nbOneToilet.unit.code, style.roomOutline.blue);
    let toiletWindow = getFeature(nbOneToilet.features, 'window');
    toiletWindow = makeWindows(canvas1, toiletWindow, style.windowStyle.default);
    let toiletDoor = getFeature(nbOneToilet.features, 'door');
    toiletDoor = makeDoor(canvas1, toiletDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    toilet.addFeature(zeroOrigin, toiletDoor, toiletWindow);
    nb.addRoom(nbOneToilet.unit.origin, toilet);

    const bath = new linea.Room(canvas1, zeroOrigin, nbOneBath.outline, nbOneBath.unit.code, style.roomOutline.blue);
    let bathWindow = getFeature(nbOneBath.features, 'window');
    bathWindow = makeWindows(canvas1, bathWindow, style.windowStyle.default);
    let bathDoor = getFeature(nbOneBath.features, 'door');
    bathDoor = makeDoor(canvas1, bathDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    bath.addFeature(zeroOrigin, bathWindow, bathDoor);
    nb.addRoom(nbOneBath.unit.origin, bath);

    const storage = new linea.Room(canvas1, zeroOrigin, nbOneStorage.outline, nbOneStorage.unit.code, style.roomOutline.blue);
    let stoDoor = getFeature(nbOneStorage.features, 'door');
    stoDoor = makeDoor(canvas1, stoDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    storage.addFeature(zeroOrigin, stoDoor);
    nb.addRoom(nbOneStorage.unit.origin, storage);

    const bedTwo = new linea.Room(canvas1, zeroOrigin, nbOneBed2.outline, nbOneBed2.unit.code, style.roomOutline.red);
    let bedTwoWindow = getFeature(nbOneBed2.features, 'window');
    bedTwoWindow = makeWindows(canvas1, bedTwoWindow, style.windowStyle.default);
    let bedTwoDoor = getFeature(nbOneBed2.features, 'door');
    bedTwoDoor = makeDoor(canvas1, bedTwoDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    bedTwo.addFeature(zeroOrigin, bedTwoDoor, bedTwoWindow);
    nb.addRoom(nbOneBed2.unit.origin, bedTwo);

    const foyer = new linea.Room(canvas1, zeroOrigin, nbOneFoyer.outline, nbOneFoyer.unit.code, style.roomOutline.gray);
    let foyerWindow = getFeature(nbOneFoyer.features, 'window');
    foyerWindow = makeWindows(canvas1, foyerWindow, style.windowStyle.default);
    let foyerDoor = getFeature(nbOneFoyer.features, 'door');
    foyerDoor = makeDoor(canvas1, foyerDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    foyer.addFeature(zeroOrigin, foyerWindow, foyerDoor);
    nb.addRoom(nbOneFoyer.unit.origin, foyer);

    nb.draw();

    // #########################################################################
    // Floor 1 Labels
    // #########################################################################

    const labels1 = [
      {x: 56, y: 40, label: 'Patio'},
      {x: 69, y: 145, label: 'Kitchen'},
      {x: 180, y: 150, label: 'Dining'},
      {x: 75, y: 265, label: 'Bedroom 2'},
      {x: 90, y: 370, label: 'Toilet'},
      {x: 97, y: 427, label: 'Full Bath'},
      {x: 82, y: 550, label: 'Bedroom 1'},
      {x: 182, y: 610, label: 'Foyer'},
    ];

    const labels1V = [
      {x: 204, y: 317, label: 'Laundry'},
      {x: 204, y: 396, label: 'Storage'},
    ];

    write(nb, zeroOrigin, labels1, style.labelStyle);
    write(nb, zeroOrigin, labels1V, style.labelStyleV);
  }
  // #########################################################################
  // Floor 2
  // #########################################################################

  if ($('#nott-floor2').length) {
    const canvas2 = new linea.LineaCanvas('#nott-floor2', -5, -5, 250, 700);
    const nb2 = new linea.Floorplan(canvas2, zeroOrigin, nbFloorTwo.room.outline, nbFloorTwo.unit.code, style.floorOutline.default);

    const wall2 = new linea.Room(canvas2, zeroOrigin, nbTwoWall.outline, nbTwoWall.unit.code, style.roomOutline.orange);
    let intWall1 = getFeature(nbTwoWall.features, 'interiorWall');
    intWall1 = makeInteriorWall(canvas2, intWall1, style.interiorWallStyle.default);
    let stairs2 = getFeature(nbTwoWall.features, 'stairs');
    stairs2 = makeStairs(canvas2, stairs2, style.stairStyle.default);
    wall2.addFeature(zeroOrigin, intWall1, stairs2);
    nb2.addRoom(nbTwoWall.unit.origin, wall2);

    const twoBed1 = new linea.Room(canvas2, zeroOrigin, nbTwoBed1.outline, nbTwoBed1.unit.code, style.roomOutline.red);
    let twoBed1Window = getFeature(nbTwoBed1.features, 'window');
    twoBed1Window = makeWindows(canvas2, twoBed1Window, style.windowStyle.default);
    let twoBed1Door = getFeature(nbTwoBed1.features, 'door');
    twoBed1Door = makeDoor(canvas2, twoBed1Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoBed1.addFeature(zeroOrigin, twoBed1Door, twoBed1Window);
    nb2.addRoom(nbTwoBed1.unit.origin, twoBed1);

    const twoBed2 = new linea.Room(canvas2, zeroOrigin, nbTwoBed2.outline, nbTwoBed2.unit.code, style.roomOutline.purple);
    let twoBed2Window = getFeature(nbTwoBed2.features, 'window');
    twoBed2Window = makeWindows(canvas2, twoBed2Window, style.windowStyle.default);
    let twoBed2Door = getFeature(nbTwoBed2.features, 'door');
    twoBed2Door = makeDoor(canvas2, twoBed2Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoBed2.addFeature(zeroOrigin, twoBed2Door, twoBed2Window);
    nb2.addRoom(nbTwoBed2.unit.origin, twoBed2);

    const twoBed3 = new linea.Room(canvas2, zeroOrigin, nbTwoBed3.outline, nbTwoBed3.unit.code, style.roomOutline.red);
    let twoBed3Window = getFeature(nbTwoBed3.features, 'window');
    twoBed3Window = makeWindows(canvas2, twoBed3Window, style.windowStyle.default);
    let twoBed3Door = getFeature(nbTwoBed3.features, 'door');
    twoBed3Door = makeDoor(canvas2, twoBed3Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoBed3.addFeature(zeroOrigin, twoBed3Door, twoBed3Window);
    nb2.addRoom(nbTwoBed3.unit.origin, twoBed3);

    const twoShower = new linea.Room(canvas2, zeroOrigin, nbTwoShower.outline, nbTwoShower.unit.code, style.roomOutline.blue);
    let twoShowerWindow = getFeature(nbTwoShower.features, 'window');
    twoShowerWindow = makeWindows(canvas2, twoShowerWindow, style.windowStyle.default);
    let twoShowerDoor = getFeature(nbTwoShower.features, 'door');
    twoShowerDoor = makeDoor(canvas2, twoShowerDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoShower.addFeature(zeroOrigin, twoShowerDoor, twoShowerWindow);
    nb2.addRoom(nbTwoShower.unit.origin, twoShower);

    const twoToilet = new linea.Room(canvas2, zeroOrigin, nbTwoToilet.outline, nbTwoToilet.unit.code, style.roomOutline.blue);
    let twoToiletWindow = getFeature(nbTwoToilet.features, 'window');
    twoToiletWindow = makeWindows(canvas2, twoToiletWindow, style.windowStyle.default);
    let twoToiletDoor = getFeature(nbTwoToilet.features, 'door');
    twoToiletDoor = makeDoor(canvas2, twoToiletDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoToilet.addFeature(zeroOrigin, twoToiletDoor, twoToiletWindow);
    nb2.addRoom(nbTwoToilet.unit.origin, twoToilet);

    const twoBath = new linea.Room(canvas2, zeroOrigin, nbTwoBath.outline, nbTwoBath.unit.code, style.roomOutline.blue);
    let twoBathWindow = getFeature(nbTwoBath.features, 'window');
    twoBathWindow = makeWindows(canvas2, twoBathWindow, style.windowStyle.default);
    let twoBathDoor = getFeature(nbTwoBath.features, 'door');
    twoBathDoor = makeDoor(canvas2, twoBathDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoBath.addFeature(zeroOrigin, twoBathDoor, twoBathWindow);
    nb2.addRoom(nbTwoBath.unit.origin, twoBath);

    const twoBed4 = new linea.Room(canvas2, zeroOrigin, nbTwoBed4.outline, nbTwoBed4.unit.code, style.roomOutline.red);
    let twoBed4Window = getFeature(nbTwoBed4.features, 'window');
    twoBed4Window = makeWindows(canvas2, twoBed4Window, style.windowStyle.default);
    let twoBed4Door = getFeature(nbTwoBed4.features, 'door');
    twoBed4Door = makeDoor(canvas2, twoBed4Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoBed4.addFeature(zeroOrigin, twoBed4Door, twoBed4Window);
    nb2.addRoom(nbTwoBed4.unit.origin, twoBed4);

    const twoBed5 = new linea.Room(canvas2, zeroOrigin, nbTwoBed5.outline, nbTwoBed5.unit.code, style.roomOutline.red);
    let twoBed5IntWall = getFeature(nbTwoBed5.features, 'interiorWall');
    twoBed5IntWall = makeInteriorWall(canvas2, twoBed5IntWall, style.interiorWallStyle.default);
    let twoBed5Window = getFeature(nbTwoBed5.features, 'window');
    twoBed5Window = makeWindows(canvas2, twoBed5Window, style.windowStyle.default);
    let twoBed5Door = getFeature(nbTwoBed5.features, 'door');
    twoBed5Door = makeDoor(canvas2, twoBed5Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    twoBed5.addFeature(zeroOrigin, twoBed5IntWall, twoBed5Door, twoBed5Window);
    nb2.addRoom(nbTwoBed5.unit.origin, twoBed5);

    nb2.draw();

    // #########################################################################
    // Floor 2 Labels
    // #########################################################################

    const labels2 = [
      {x: 166, y: 88, label: 'Bedroom 6'},
      {x: 65, y: 154, label: 'Media\nRoom'},
      {x: 70, y: 290, label: 'Bedroom 5'},
      {x: 87, y: 370, label: 'Toilet'},
      {x: 92, y: 430, label: 'Full Bath'},
      {x: 67, y: 547, label: 'Bedroom 3'},
      {x: 169, y: 575, label: 'Bedroom 4'},
    ];

    const labels2V = [
      {x: 201, y: 184, label: 'Shower'},
    ];

    write(nb2, zeroOrigin, labels2, style.labelStyle);
    write(nb2, zeroOrigin, labels2V, style.labelStyleV);
  }
  // #########################################################################
  // Floor 3
  // #########################################################################

  if ($('#nott-floor3').length) {
    const canvas3 = new linea.LineaCanvas('#nott-floor3', -5, -5, 250, 700);
    const nb3 = new linea.Floorplan(canvas3, zeroOrigin, nbFloorThree.room.outline, nbFloorThree.unit.code, style.floorOutline.default);

    const wall3 = new linea.Room(canvas3, zeroOrigin, nbThreeWall.outline, nbThreeWall.unit.code, style.roomOutline.orange);
    let intWall3 = getFeature(nbThreeWall.features, 'interiorWall');
    intWall3 = makeInteriorWall(canvas3, intWall3, style.interiorWallStyle.default);
    let stairs3 = getFeature(nbThreeWall.features, 'stairs');
    stairs3 = makeStairs(canvas3, stairs3, style.stairStyle.default);
    wall3.addFeature(zeroOrigin, intWall3, stairs3);
    nb3.addRoom(nbThreeWall.unit.origin, wall3);

    const threeBed1 = new linea.Room(canvas3, zeroOrigin, nbThreeBed1.outline, nbThreeBed1.unit.code, style.roomOutline.red);
    let threeBed1Window = getFeature(nbThreeBed1.features, 'window');
    threeBed1Window = makeWindows(canvas3, threeBed1Window, style.windowStyle.default);
    let threeBed1Door = getFeature(nbThreeBed1.features, 'door');
    threeBed1Door = makeDoor(canvas3, threeBed1Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeBed1.addFeature(zeroOrigin, threeBed1Door, threeBed1Window);
    nb3.addRoom(nbThreeBed1.unit.origin, threeBed1);

    const threeBed2 = new linea.Room(canvas3, zeroOrigin, nbThreeBed2.outline, nbThreeBed2.unit.code, style.roomOutline.red);
    let threeBed2Win = getFeature(nbThreeBed2.features, 'window');
    let threeBed2Door = getFeature(nbThreeBed2.features, 'door');
    threeBed2Win = makeWindows(canvas3, threeBed2Win, style.windowStyle.default);
    threeBed2Door = makeDoor(canvas3, threeBed2Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeBed2.addFeature(zeroOrigin, threeBed2Door, threeBed2Win);
    nb3.addRoom(nbThreeBed2.unit.origin, threeBed2);

    const threeBed3 = new linea.Room(canvas3, zeroOrigin, nbThreeBed3.outline, nbThreeBed3.unit.code, style.roomOutline.red);
    let threeBed3Win = getFeature(nbThreeBed3.features, 'window');
    let threeBed3Door = getFeature(nbThreeBed3.features, 'door');
    threeBed3Win = makeWindows(canvas3, threeBed3Win, style.windowStyle.default);
    threeBed3Door = makeDoor(canvas3, threeBed3Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeBed3.addFeature(zeroOrigin, threeBed3Door, threeBed3Win);
    nb3.addRoom(nbThreeBed3.unit.origin, threeBed3);

    const threeShower = new linea.Room(canvas3, zeroOrigin, nbThreeShower.outline, nbThreeShower.unit.code, style.roomOutline.blue);
    let threeShowerWin = getFeature(nbThreeShower.features, 'window');
    let threeShowerDoor = getFeature(nbThreeShower.features, 'door');
    threeShowerWin = makeWindows(canvas3, threeShowerWin, style.windowStyle.default);
    threeShowerDoor = makeDoor(canvas3, threeShowerDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeShower.addFeature(zeroOrigin, threeShowerDoor, threeShowerWin);
    nb3.addRoom(nbThreeShower.unit.origin, threeShower);

    const threeToilet = new linea.Room(canvas3, zeroOrigin, nbThreeToilet.outline, nbThreeToilet.unit.code, style.roomOutline.blue);
    let threeToiWin = getFeature(nbThreeToilet.features, 'window');
    let threeToiDoor = getFeature(nbThreeToilet.features, 'door');
    threeToiWin = makeWindows(canvas3, threeToiWin, style.windowStyle.default);
    threeToiDoor = makeDoor(canvas3, threeToiDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeToilet.addFeature(zeroOrigin, threeToiDoor, threeToiWin);
    nb3.addRoom(nbThreeToilet.unit.origin, threeToilet);

    const threeBath = new linea.Room(canvas3, zeroOrigin, nbThreeBath.outline, nbThreeBath.unit.code, style.roomOutline.blue);
    let threeBathWin = getFeature(nbThreeBath.features, 'window');
    let threeBathDoor = getFeature(nbThreeBath.features, 'door');
    threeBathWin = makeWindows(canvas3, threeBathWin, style.windowStyle.default);
    threeBathDoor = makeDoor(canvas3, threeBathDoor,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeBath.addFeature(zeroOrigin, threeBathDoor, threeBathWin);
    nb3.addRoom(nbThreeBath.unit.origin, threeBath);

    const threeBed4 = new linea.Room(canvas3, zeroOrigin, nbThreeBed4.outline, nbThreeBed4.unit.code, style.roomOutline.red);
    let threeBed4Win = getFeature(nbThreeBed4.features, 'window');
    let threeBed4Door = getFeature(nbThreeBed4.features, 'door');
    threeBed4Win = makeWindows(canvas3, threeBed4Win, style.windowStyle.default);
    threeBed4Door = makeDoor(canvas3, threeBed4Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeBed4.addFeature(zeroOrigin, threeBed4Door, threeBed4Win);
    nb3.addRoom(nbThreeBed4.unit.origin, threeBed4);

    const threeBed5 = new linea.Room(canvas3, zeroOrigin, nbThreeBed5.outline, nbThreeBed5.unit.code, style.roomOutline.red);
    let threeBed5Win = getFeature(nbThreeBed5.features, 'window');
    let threeBed5Door = getFeature(nbThreeBed5.features, 'door');
    threeBed5Win = makeWindows(canvas3, threeBed5Win, style.windowStyle.default);
    threeBed5Door = makeDoor(canvas3, threeBed5Door,  style.doorStyle.door.default, style.doorStyle.projection.default, style.doorStyle.doorStop.default);
    threeBed5.addFeature(zeroOrigin, threeBed5Door, threeBed5Win);
    nb3.addRoom(nbThreeBed5.unit.origin, threeBed5);

    nb3.draw();

    // #########################################################################
    // Floor 3 Labels
    // #########################################################################

    const labels3 = [
      {x: 174, y: 87, label: 'Bedroom 11'},
      {x: 65, y: 147, label: 'Bedroom 10'},
      {x: 70, y: 280, label: 'Bedroom 9'},
      {x: 82, y: 370, label: 'Toilet'},
      {x: 85, y: 439, label: 'Full Bath'},
      {x: 188, y: 427, label: 'Kitchen'},
      {x: 67, y: 566, label: 'Bedroom 7'},
      {x: 175, y: 566, label: 'Bedroom 8'},
    ];
    const labels3V = [
      {x: 202, y: 337, label: 'Shower'},
    ];

    write(nb3, zeroOrigin, labels3, style.labelStyle);
    write(nb3, zeroOrigin, labels3V, style.labelStyleV);
  }
});
