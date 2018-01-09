const nbOneBath = {
  unit: {
    name: 'Full Bath',
    code: '6-n-f1-r5',
    rate: 0,
    number: 4,
    scale: 1,
    origin: { x: 63.2, y: 387.2 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 72.2, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 72.2, y: 70.3, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 70.3, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f1-r5-d1',
      label: 'Door',
      origin: { x: 0, y: 0},
      clockwise: true,
      outline: [
        { x: 72.2, y: 4, index: 0 },
        { x: 72.2, y: 34.2, index: 1 },
      ],
    },
    {
      type: 'window',
      code: '6-n-f1-r5-w1',
      label: 'Window',
      origin: { x: 0, y: 0},
      outline: [
        { x: -12.2, y: 2.6, radius: 0, curve: 'none', index: 0 },
        { x: -12.2, y: 32.6, radius:0, curve: 'none', index: 1 },
      ],
    }
  ],
};
