const nbOneStorage = {
  unit: {
    name: 'Storage',
    code: '6-n-f1-r6',
    rate: 0,
    number: 6,
    scale: 1,
    origin: { x: 185.3, y: 349 }
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 35.6, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 35.6, y: 91.5, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 91.5, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f1-r6-d1',
      label: 'Door',
      origin: { x: 0, y: 0},
      clockwise: true,
      outline: [
        { x: 0, y: 20, index: 0 },
        { x: 0, y: 42.7, index: 1 },
      ],
    },
  ]
};
