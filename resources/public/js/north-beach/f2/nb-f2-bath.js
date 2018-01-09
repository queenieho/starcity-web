const nbTwoBath = {
  unit: {
    name: 'Full Bath',
    code: '6-n-f2-r6',
    rate: 0,
    number: 1,
    scale: 1,
    origin: { x: 54.7, y: 387 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 80.3, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 80.3, y: 70, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 70, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f2-r6-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: -7.7, y: 2.5, radius: 0, curve: 'none', index: 0,
        },
        {
          x: -7.7, y: 32.5, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r6-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 80.3, y: 4, index: 0 },
        { x: 80.3, y: 34, index: 1 },
      ],
    },
  ],
};
