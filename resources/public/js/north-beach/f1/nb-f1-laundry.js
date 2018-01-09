const nbOneLaundry = {
  unit: {
    name: 'Laundry',
    code: '6-n-f1-r3',
    rate: 0,
    number: 1,
    scale: 1,
    origin: { x: 184.7, y: 285.6 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 36.2, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 36.2, y: 58.4, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 58.4, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f1-r3-d1',
      label: 'Door',
      origin: { x: 0, y: 0},
      clockwise: false,
      outline: [
        { x: 1.7, y: 0, index: 0 },
        { x: 33.7, y: 0, index: 1 },
      ],
    },
  ],
};
