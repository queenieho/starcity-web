const nbTwoBed3 = {
  unit: {
    name: 'Bedroom 3',
    code: '6-n-f2-r4',
    rate: 2000,
    numer: 1,
    scale: 1,
    origin: { x: 16, y: 220.8 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 119.4, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 119.4, y: 123.2, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 38.8, y: 123.2, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 0, y: 84.2, radius: 0, curve: 'none', index: 4,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f2-r4-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 0.7, y: 96, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 27, y: 122.2, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r4-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 119, y: 42, index: 0 },
        { x: 119, y: 71.2, index: 1 },
      ],
    },
  ],
};
