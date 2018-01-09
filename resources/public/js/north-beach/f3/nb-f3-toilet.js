const nbThreeToilet = {
  unit: {
    name: 'Toilet',
    code: '6-n-f3-r5',
    ratE: 0,
    number: 1,
    scale: 1,
    origin: { x: 54.7, y: 348.7 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 80.7, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 80.7, y: 35.2, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 35.2, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f3-r5-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: -7.8, y: 8.8, radius: 0, curve: 'none', index: 0,
        },
        {
          x: -7.8, y: 32.1, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f3-r5-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 80.7, y: 4.3, index: 0 },
        { x: 80.7, y: 27.7, index: 1 },
      ],
    },
  ],
};
