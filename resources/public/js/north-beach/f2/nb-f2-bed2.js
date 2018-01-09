const nbTwoBed2 = {
  unit: {
    name: 'Bedroom 2',
    code: '6-n-f2-r2',
    rate: 2000,
    number: 1,
    scale: 1,
    origin: { x: 16, y: 86.7 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 97, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 97, y: 81.1, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 119, y: 81.1, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 119, y: 129.3, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 0, y: 129.3, radius: 0, curve: 'none', index: 5,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f2-r2-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 20.6, y: -7.5, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 70, y: -7.5, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r2-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 119, y: 84.3, index: 0 },
        { x: 119, y: 113.8, index: 1 },
      ],
    },
  ],
};
