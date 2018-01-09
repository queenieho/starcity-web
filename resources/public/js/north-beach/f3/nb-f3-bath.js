const nbThreeBath = {
  unit: {
    name: 'Full Bath',
    code: '6-n-f3-r6',
    rate: 0,
    number: 1,
    scale: 1,
    origin: { x: 54.7, y: 388.7 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 62.3, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 62.3, y: 67.3, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 32.1, y: 97.3, radius: 0, curve: 'none', index: 3,
    },
    {
      x: -38.7, y: 97.3, radius: 0, curve: 'none', index: 4,
    },
    {
      x: -38.7, y: 61.9, radius: 0, curve: 'none', index: 5,
    },
    {
      x: 0, y: 61.9, radius: 0, curve: 'none', index: 6,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f3-r6-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: -7.8, y: 0.8, radius: 0, curve: 'none', index: 0,
        },
        {
          x: -7.8, y: 30.8, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f3-r6-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 62.4, y: 6, index: 0 },
        { x: 62.4, y: 36.1, index: 1 },
      ],
    },
  ],
};
