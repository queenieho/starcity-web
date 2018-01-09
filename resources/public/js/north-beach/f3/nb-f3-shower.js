const nbThreeShower = {
  unit: {
    name: 'Shower',
    code: '6-n-f3-r4',
    rate: 2000,
    number: 1,
    scale: 1,
    origin: { x: 182.8, y: 301.5 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 42.2, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 42.2, y: 68.5, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 68.5, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f3-r4-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: 'false',
      outline: [
        { x: 0, y: 35.3, index: 0 },
        { x: 0, y: 65.4, index: 1 },
      ],
    },
  ],
};
