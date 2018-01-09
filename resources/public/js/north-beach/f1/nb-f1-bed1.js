const nbOneBed1 = {
  unit: {
    name: 'Bedroom 01',
    code: '6-n-f1-r2',
    rate: 2000, // still to be defined
    number: 1,
    scale: 1,
    origin: { x: 20.3, y: 199.8 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 103.9, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 103.9, y: 144.4, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 35.5, y: 144.4, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 0, y: 109, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 0, y: 38, radius: 0, curve: 'none', index: 5,
    },
    {
      x: 2.2, y: 38, radius: 0, curve: 'none', index: 6,
    },
    {
      x: 2.2, y: 13.2, radius: 0, curve: 'none', index: 7,
    },
    {
      x: 0, y: 13.2, radius: 0, curve: 'none', index: 8,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f1-r2-w1',
      label: 'Window',
      origin: { x: 0, y: 0},
      outline: [
        {
          x: -3.4, y: 117.3, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 22.5, y: 143.3, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f1-r2-d1',
      label: 'Door',
      origin: { x: 0, y: 0},
      clockwise: false,
      outline: [
        { x: 103.7, y: 140.2, index: 0 },
        { x: 103.7, y: 109.9, index: 1 },
      ],
    },
  ],
};
