const nbOneToilet = {
  unit: {
    name: 'Toilet',
    code: '6-n-f1-r4',
    rate: 0,
    number: 4,
    scale: 1,
    origin: { x: 63.2, y: 349 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 72.2, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 72.2, y: 33.3, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 33.3, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f1-r4-d1',
      label: 'Door',
      origin: { x: 0, y: 0},
      clockwise: true,
      outline: [
        { x: 72.2, y: 4.5, index: 0 },
        { x: 72.2, y: 28, index: 1 },
      ],
    },
    {
      type: 'window',
      code: '6-n-f1-r4-w1',
      label: 'Window',
      origin: { x: 0, y: 0},
      outline: [
        { x: -12.2, y: 9.3, radius: 0, curve: 'none', index: 0 },
        { x: -12.2, y: 32.6, radius:0, curve: 'none', index: 1 },
      ],
    }
  ],
};
