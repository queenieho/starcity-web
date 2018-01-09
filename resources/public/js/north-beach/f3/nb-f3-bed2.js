const nbThreeBed2 = {
  unit: {
    name: 'Bedroom 2',
    code: '6-n-f3-r2',
    rate: 2000,
    number: 1,
    scale: 1,
    origin: { x: 21, y: 86.7 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 92, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 92, y: 81.1, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 114.4, y: 81.1, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 114.4, y: 129.3, radius: 0, curve: 'none', index: 4,
    },
    {
      x: -5, y: 129.3, radius: 0, curve: 'none', index: 5,
    },
    {
      x: -5, y: 31.3, radius: 0, curve: 'none', index: 6,
    },
    {
      x: 0, y: 31.1, radius: 0, curve: 'none', index: 7,
    },
  ],
  features: [
    {
      type:'window',
      code: '6-n-f3-r2-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 15.6, y: -7.6, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 45.1, y: -7.6, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f3-r2-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        { x: 111.4, y: 84.1, index: 0 },
        { x: 111.4, y: 113.6, index: 1 },
      ],
    },
  ],
};
