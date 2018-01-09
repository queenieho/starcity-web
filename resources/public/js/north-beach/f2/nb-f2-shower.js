const nbTwoShower = {
  unit: {
    name: 'Shower',
    code: '6-n-f1-r3',
    rate: 0,
    number: 0,
    scale: 1,
    origin: { x: 184.8, y: 154 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 32.2, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 32.2, y: 59, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 59, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f2-r3-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 0, y: 32, index: 0 },
        { x: 0, y: 56, index: 1 },
      ],
    },
  ],
};
