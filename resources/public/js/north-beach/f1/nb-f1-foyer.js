const nbOneFoyer = {
  unit: {
    name: 'Foyer',
    code: '6-n-f1-r8',
    rate: 0,
    number: 8,
    scale: 1,
    origin: { x: 141.2, y: 576.9 },
  },
  outline: [
    {
      x: 0, y: 0, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 79.7, y: 0, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 79.7, y: 61.5, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 0, y: 61.5, radius: 0, curve: 'none', index: 3,
    },
  ],
  features: [
    {
      type: 'window',
      code: '6-n-f1-r8-w1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 47.2, y: -3, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 76.2, y: -3, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f1-r8-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: false,
      outline: [
        { x: 4.5, y: 0, index: 0 },
        { x: 33.5, y: 0, index: 1 },
      ],
    },
  ],
};
