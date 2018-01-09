const nbThreeBed5 = {
  unit: {
    name: 'Bedroom 5',
    code: '6-n-f3-r8',
    rate: 2300,
    number: 1,
    scale: 1,
    origin: { x: 153.7, y: 490.6 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 71.3, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 71.3, y: 150.4, radius: 0, curve: 'none', index: 2,
    },
    {
      x: -29.9, y: 150.4, radius: 0, curve: 'none', index: 3,
    },
    {
      x: -29.9, y: 29.4, radius: 0, curve: 'none', index: 4,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f3-r8-w1',
      label: 'Window',
      origin: {x: 0, y: 0 },
      outline: [
        {
          x: -4, y: 158.1, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 45.4, y: 158.1, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f3-r8-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: false,
      outline: [
        { x: -3.5, y: 3.5, index: 0 },
        { x: -26.3, y: 26.1, index: 1 },
      ],
    },
  ],
};
