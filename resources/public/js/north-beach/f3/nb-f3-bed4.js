const nbThreeBed4 = {
  unit: {
    name: 'Bedroom 4',
    code: '6-n-f3-r7',
    rate: 2300,
    number: 1,
    scale: 1,
    origin: { x: 16, y: 490.6 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 73.2, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 103, y: 29.8, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 103, y: 150.4, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 0, y: 150.4, radius: 0, curve: 'none', index: 4,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f3-r7-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 26.7, y: 158.1, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 76.3, y: 158.1, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f3-r7-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 76.8, y: 3.5, index: 0 },
        { x: 99.4, y: 26.1, index: 1 },
      ],
    },
  ],
};
