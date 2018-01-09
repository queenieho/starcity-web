const nbOneWall = {
  unit: {
    name: 'Wall',
    code: '6-n-f1-r0',
    rate: 0,
    number: 1,
    scale: 1,
    origin: { x: 0, y: 0 },
  },
  outline: [
    {
      x: 123.2, y: 23.6, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 220.7, y: 23.6, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 220.7, y: 280.7, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 179, y: 280.7, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 179, y: 344.2, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 175.4, y: 344.2, radius: 0, curve: 'none', index: 5,
    },
    {
      x: 175.4, y: 349, radius: 0, curve: 'none', index: 6,
    },
    {
      x: 179, y: 349, radius: 0, curve: 'none', index: 7,
    },
    {
      x: 179, y: 450, radius: 0, curve: 'none', index: 8,
    },
    {
      x: 220.7, y: 450, radius: 0, curve: 'none', indeX: 9,
    },
    {
      x: 221, y: 571, radius: 0, curve: 'none', index: 10,
    },
    {
      x: 141.2, y: 571, radius: 0, curve: 'none', index: 11,
    },
    {
      x: 141.2, y: 349, radius: 0, curve: 'none', index: 12,
    },
    {
      x: 146, y: 349, radius: 0, curve: 'none', index: 13,
    },
    {
      x: 146, y: 344.2, radius: 0, curve: 'none', index: 14,
    },
    {
      x: 129, y: 344.2, radius: 0, curve: 'none', index: 15,
    },
    {
      x: 129, y: 194.8, radius: 0, curve: 'none', index: 16,
    },
    {
      x: 20.4, y: 194.8, radius: 0, curve: 'none', index: 17,
    },
    {
      x: 20.4, y: 90, radius: 0, curve: 'none', index: 18,
    },
    {
      x: 123.4, y: 90, radius: 0, curve: 'none', index: 19,
    },
    // {
    //   x: 123.2, y: 23.6, radius: 0, curve: 'none', index: 0,
    // },
    // {
    //   x: 220.7, y: 23.6, radius: 0, curve: 'none', index: 1,
    // },
    // {
    //   x: 221, y: 638.4, radius: 0, curve: 'none', index: 2,
    // },
    // {
    //   x: 23.4, y: 638.4, radius: 0, curve: 'none', index: 3,
    // },
    // {
    //   x: 23.4, y: 455, radius: 0, curve: 'none', index: 4,
    // },
    // {
    //   x: 63.2, y: 455, radius: 0, curve: 'none', index: 5,
    // },
    // {
    //   x: 63.2, y: 351.6, radius: 0, curve: 'none', index: 6,
    // },
    // {
    //   x: 20.4, y: 308.8, radius: 0, curve: 'none', index: 7,
    // },
    // {
    //   x: 20.4, y: 237.8, radius: 0, curve: 'none', index: 8,
    // },
    // {
    //   x: 22.5, y: 237.8, radius: 0, curve: 'none', index: 9,
    // },
    // {
    //   x: 22.5, y: 213, radius: 0, curve: 'none', index: 10,
    // },
    // {
    //   x: 20.4, y: 213, radius: 0, curve: 'none', index: 11,
    // },
    // {
    //   x: 20.4, y: 90, radius: 0, curve: 'none', index: 12,
    // },
    // {
    //   x: 123.4, y: 90, radius: 0, curve: 'none', index: 13,
    // },
  ],
  features: [
    {
      type: 'door',
      code: '6-n-f1-r0-d1',
      label: 'door',
      clockwise: false,
      origin: { x: 0, y: 0 },
      outline: [
        { x: 0, y: 36, index: 0 },
        { x: 0, y: 12, index: 1 },
      ],
    },
    {
      type: 'stairs',
      code: '6-n-f1-r0-st1',
      origin: { x: 0, y: 0 },
      vertical: false,
      outline: [
        { x: 185.4, y: 450, index: 0 },
        { x: 220.9, y: 450, index: 1 },
        { x: 220.9, y: 510, index: 2 },
        { x: 185.4, y: 510, index: 3 },
      ],
    },
  ],
};
