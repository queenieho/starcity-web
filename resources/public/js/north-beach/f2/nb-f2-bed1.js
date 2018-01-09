const nbTwoBed1 = {
  unit: {
    name: 'Bedroom 01',
    code: '6-n-f2-r1',
    rate: 2000,
    number: 1,
    scale: 1,
    origin: { x: 118.9, y: 16 }
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 98.7, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 98.7, y: 133, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 60.5, y: 133, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 60.5, y: 147, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 0, y: 147, radius: 0, curve: 'none', index: 5,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f2-r1-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: -8, y: 15.7, radius: 0, curve: 'none', index: 0,
        },
        {
          x: -8, y: 48.3, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f2-r1-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: false,
      outline: [
        { x: 23.3, y: 147, index: 0 },
        { x: 52.5, y: 147, index: 1 },
      ],
    },
  ],
};
