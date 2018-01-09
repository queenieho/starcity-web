const nbThreeWall = {
  unit: {
    name: 'Wall',
    code: '6-n-f3-r0',
    rate: 0,
    number: 1,
    scale: 1,
    origin: { x: 0, y: 0 },
  },
  outline: [
    {
      x: 141.2, y: 167.7, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 225, y: 167.7, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 225, y: 295.9, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 177.3, y: 295.9, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 177.3, y: 375, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 225, y: 375, radius: 0, curve: 'none', index: 5,
    },
    {
      x: 225, y: 485.6, radius: 0, curve: 'none', index: 6,
    },
    {
      x: 151.6, y: 485.6, radius: 0, curve: 'none', index: 7,
    },
    {
      x: 121.5, y: 515.8, radius: 0, curve: 'none', index: 8,
    },
    {
      x: 92.8, y: 487, radius: 0, curve: 'none', index: 9,
    },
    {
      x: 122.6, y: 457.2, radius: 0, curve: 'none', index: 10,
    },
    {
      x: 122.6, y: 388.7, radius: 0, curve: 'none', index: 11,
    },
    {
      x: 141.2, y: 388.7, radius: 0, curve: 'none', index: 12,
    },
  ],
  features: [
    {
      type: 'interiorWall',
      code: '6-n-f3-r0-int1',
      label: 'Interior Wall',
      origin: { x: 0, y: 0 },
      outline: [
        { x: 182.8, y: 206.5, radius: 0, curve: 'none', index: 0 },
        { x: 182.8, y: 301.5, radius: 0, curve: 'none', index: 1 },
      ],
    },
    {
      type: 'interiorWall',
      code: '6-n-f3-r0-int2',
      label: 'Interior Wall',
      origin: { x: 0, y: 0 },
      outline: [
        { x: 223, y: 167.7, radius: 0, curve: 'none', index: 0 },
        { x: 223, y: 301.5, radius: 0, curve: 'none', index: 1 },
      ],
    },
    {
      type: 'stairs',
      code: '6-n-f3-r0-st1',
      label: 'Stairs',
      origin: { x: 0, y: 0 },
      vertical: false,
      outline: [
        { x: 182.8, y: 210, index: 0 },
        { x: 220.5, y: 210, index: 1 },
        { x: 220.5, y: 296, index: 2 },
        { x: 182.8, y: 296, index: 3 },
      ],
    },
  ],
};
