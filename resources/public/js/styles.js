// Styles to be used in rendering our floorplans
const style = {
  labelStyle: {
    fill: '#555',
    fontFamily: 'Work Sans',
    fontStyle: 'Sans Serif',
    textAnchor: 'middle',
    alignmentBaseline: 'middle',
    fontSize: 12,
  },
  labelStyleV: {
    fill: '#555',
    fontFamily: 'Work Sans',
    fontStyle: 'Sans Serif',
    textAnchor: 'Middle',
    alignmentBaseline: 'middle',
    writingMode: 'tb',
    fontSize: 12,
  },
  circleStyle: {
    stroke: 'black',
  },
  gridStyle: {
    major: {
      stroke: '#cccccc',
      strokeWidth: 1,
    },
    minor: {
      stroke: '#eaeaea',
      strokeWidth: 1,
    },
  },
  floorOutline: {
    default: {
      stroke: '#666',
      strokeWidth: 0,
      fill: '#999',
    },
  },
  roomOutline: {
    orange: {
      stroke: '#666',
      strokeWidth: 0,
      fill: '#fef3df',
      fillOpacity: 1,
    },
    red: {
      // stroke: '#666',
      strokeWidth: 2,
      fill: '#fcebcc',
      fillOpacity: 1,
    },
    blue: {
      // stroke: '#666',
      strokeWidth: 2,
      fill: '#dcedf7',
      fillOpacity: 1,
    },
    green: {
      // stroke: '#666',
      strokeWidth: 2,
      fill: '#dcf7e6',
      fillOpacity: 1,
    },
    purple: {
      // stroke: '#666',
      strokeWidth: 2,
      fill: '#f3def9',
      fillOpacity: 1,
    },
    gray: {
      // stroke: '#666',
      strokeWidth: 2,
      fill: '#eaeaea',
      fillOpacity: 1,
    },
  },
  interiorWallStyle: {
    default: {
      stroke: '#999',
      strokeWidth: 6,
      fill: '#f4e4d7',
      fillOpacity: 1,
    },
  },
  stairStyle: {
    default: {
      stroke: '#999',
      strokeWidth: 1,
    },
  },
  windowStyle: {
    default: {
      stroke: '#e7f2f9',
      strokeWidth: 6,
      fill: 'none',
      fillOpacity: 1,
      // strokeLinecap: 'round',
    },
  },
  doorStyle: {
    door: {
      default: {
        stroke: '#555',
        strokeWidth: 5,
        fill: 'none',
        fillOpacity: 1,
      },
    },
    projection: {
      default: {
        stroke: '#bbb',
        strokeWidth: 3,
        fill: 'none',
        fillOpacity: 1,
        strokeDasharray: '6 4',
        // strokeLinecap: 'round',
      },
    },
    doorStop: {
      default: {
        stroke: '#ababab',
        strokeWidth: 2,
        fill: 'none',
        fillOpacity: 1,
      },
      open: {
        stroke: '#ff00ff',
        strokeWidth: 1,
        fill: 'none',
        fillOpacity: 1,
      },
    },
  },
  bedStyle: {
    default: {
      stroke: '#333',
      strokeWidth: 1,
      fill: 'none',
      fillOpacity: 1,
    },
  },
  dresserStyle: {
    default: {
      stroke: 'grey',
      strokeWidth: 1,
      fill: 'none',
      fillOpacity: 1,
    },
  },
  nightTableStyle: {
    default: {
      stroke: 'grey',
      strokeWidth: 1,
      fill: 'none',
      fillOpacity: 1,
    },
  },
};
