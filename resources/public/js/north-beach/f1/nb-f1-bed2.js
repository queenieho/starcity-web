const nbOneBed2 = {
  unit: {
    name: 'Bedroom 2',
    code: '6-n-f1-r7',
    rate: 2100,
    number: 7,
    scale: 1,
    origin: { x: 23.4, y: 455 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 35, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 34.9, y: 7.6, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 112, y: 7.7, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 112, y: 183.4, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 0, y: 183.4, radius: 0, curve: 'none', index: 5,
    },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f1-r7-d1',
      label: 'Door',
      origin: { x: 0, y: 0},
      clockwise: true,
      outline: [
        { x: 112, y: 11.5, index: 0 },
        { x: 112, y: 40.7, index: 1 },
      ],
    },
    {
      type: 'window',
      code: '6-n-f1-r7-w1',
      label: 'Window',
      origin: { x: 0, y: 0},
      outline: [
        {
          x: 19, y: 192.5, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 68.4, y: 192.5, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
  ],
};
