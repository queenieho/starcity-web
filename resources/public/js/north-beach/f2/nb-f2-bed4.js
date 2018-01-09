const nbTwoBed4 = {
  unit: {
    name: 'Bedroom 4',
    code: '6-n-f2-r7',
    rate: 2300,
    number: 1,
    scale: 1,
    origin: { x: 15.7, y: 450.6 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 33.9, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 33.9, y: 11.8, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 119.3, y: 11.8, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 119.3, y: 48.2, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 103, y: 48.2, radius: 0, curve: 'none', index: 5,
    },
    {
      x: 103, y: 182.7, radius: 0, curve: 'none', index: 6,
    },
    {
      x: 0, y: 182.7, radius: 0, curve: 'none', index: 7,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f2-r7-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 26.9, y: 194.6, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 76.3, y: 194.6, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r7-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 119.3, y: 14.7, index: 0 },
        { x: 119.3, y: 44.9, index: 1},
      ],
    },
  ],
};
