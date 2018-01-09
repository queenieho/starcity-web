const nbThreeBed1 = {
  unit: {
    name: 'Bedroom 1',
    code: '6-n-f3-r1',
    rate: 2000,
    number: 1,
    scale: 1,
    origin: { x: 119, y: 16 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 106, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 106, y: 147, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 147, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f3-r1-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: -8.1, y: 15.6, radius: 0, curve: 'none', index: 0,
        },
        {
          x: -8.1, y: 48.1, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f3-r1-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 58.6, y: 147, index: 0 },
        { x: 29.6, y: 147, index: 1 },
      ],
    },
  ],
};
