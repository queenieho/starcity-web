const nbTwoBed5 = {
  unit: {
    name: 'Bedroom 5',
    code: '6-n-f2-r8',
    rate: 2500,
    number: 1,
    scale: 1,
    origin: { x: 123.8, y: 503.9 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 61.6, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 61.6, y: -21.1, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 93.6, y: -21.1, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 93.6, y: 129.4, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 0, y: 129.4, radius: 0, curve: 'none', index: 5,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f2-r8-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 25.4, y: 141.3, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 75.1, y: 141.3, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'interiorWall',
      code: '6-n-f2-r8-int1',
      label: 'Interior Wall',
      origin: { x: 0, y: 0 },
      outline: [
        { x: 61.6, y: -3, radius: 0, curve: 'none', index: 0 },
        { x: 93.6, y: -3, radius: 0, curve: 'none', index: 1 },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r8-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: false,
      outline: [
        { x: 50.2, y: 0, index: 0 },
        { x: 21.1, y: 0, index: 1 },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r8-d2',
      label: 'Closet Door',
      origin: { x: 0, y: 0 },
      clockwise: false,
      outline: [
        { x: 88.9, y: 0, index: 0, },
        { x: 65.6, y: 0, index: 1, },
      ],
    },
  ],
};
